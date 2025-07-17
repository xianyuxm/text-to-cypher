package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.xm.text_to_cypher.domain.EntityRelationVector;
import com.xm.text_to_cypher.dto.*;
import com.xm.text_to_cypher.dto.neo4j.NodeDTO;
import com.xm.text_to_cypher.dto.neo4j.PathDTO;
import com.xm.text_to_cypher.dto.neo4j.RelationshipValueDTO;
import com.xm.text_to_cypher.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.springframework.ai.embedding.Embedding;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@RequiredArgsConstructor
public class TripleToCypherExecutorImpl implements TripleToCypherExecutor {

    private final Neo4jRepository neo4jRepository;

    private static volatile CypherSchemaDTO cypherSchemaDTO;

    private final AiCallService aiCallService;



    private final EntityRelationVectorService nodeRelationVectorService;

    private final TextToSegmentService textToSegmentService;

    private final Driver driver;


    @Override
    public CypherSchemaDTO queryRelationSchema(String query) {
        if (StrUtil.isEmpty(query)){
            return new CypherSchemaDTO(List.of(), List.of());
        }
        // 对问题进行分词
        List<TextTerm> terms = textToSegmentService.segmentText(query);
        if (CollUtil.isEmpty(terms)){
            return new CypherSchemaDTO(List.of(), List.of());
        }
        log.info("queryRelationSchema: 分词结果：{}", terms);
        log.info("queryRelationSchema: 开始进行文本标签向量匹配...");
        List<EntityRelationVector> matchedText = new ArrayList<>();
        List<String> keywords = mergeNodeAndRelationLabel();
        for (TextTerm term : terms) {
            if (StrUtil.isEmpty(term.getLabelValue(keywords))){
                log.info("queryRelationSchema: 分词结果`{}`不是关键标签，跳过...", term.getWord());
                continue;
            }
            Embedding embedding = aiCallService.embedding(term.getLabelValue(keywords));
            term.setEmbedding(embedding.getOutput());
            List<EntityRelationVector> textVectorDTOS = nodeRelationVectorService.matchSimilarByCosine(embedding.getOutput(), 0.9, List.of("E","R"),3); // 继续过滤
            log.info("retrieval: 文本：`{}`匹配到的文本向量：`{}`", term.getWord() ,textVectorDTOS.stream().map(EntityRelationVector::getContent).collect(Collectors.joining(" ")));
            matchedText.addAll(textVectorDTOS);
        }
        if (CollUtil.isEmpty(matchedText)){
            log.info("retrieval: 未找到匹配的文本向量");
            return new CypherSchemaDTO(List.of(), List.of());
        }
        loadCypherSchemaIfAbsent();
        List<RelationExtractionDTO> merged = new ArrayList<>();
        for (EntityRelationVector textVectorDTO : matchedText) {
            String content = textVectorDTO.getContent();
            List<RelationExtractionDTO> relations = cypherSchemaDTO.getRelations(content);
            for (RelationExtractionDTO relation : relations) {
                boolean noneMatch = merged.stream().noneMatch(i ->
                        StrUtil.equals(i.getSourceType(), relation.getSourceType()) &&
                                StrUtil.equals(i.getRelation(), relation.getRelation()) &&
                                StrUtil.equals(i.getTargetType(), relation.getTargetType())
                );
                if (noneMatch){
                    merged.add(relation);
                }
            }
        }
        // 对查询到的关系进行重排序
        List<Pair<Double, RelationExtractionDTO>> pairs = new ArrayList<>();
        TimeInterval timeInterval = new TimeInterval();
        String join = terms.stream().map(t->t.getLabelValue(keywords)).filter(StrUtil::isNotEmpty).collect(Collectors.joining());
        Embedding embedding = aiCallService.embedding(join);
        for (RelationExtractionDTO relation : merged) {
            String content = relation.getSourceType() + " " + relation.getRelation() + " " + relation.getTargetType();
            Double score = nodeRelationVectorService.matchContentScore(embedding.getOutput(),content); // 暂时调用数据库查询进行数据匹配。目前总体耗时1秒内
            if (null == score){
                continue;
            }
            log.info("queryRelationSchema: 关系`{}`的匹配分数：{}", content, score);
            pairs.add(Pair.of(score, relation));
        }
        log.info("queryRelationSchema: 关系排序耗时：{}ms", timeInterval.intervalMs());

        merged = pairs.stream().sorted((p1, p2) -> Double.compare(p2.getKey(), p1.getKey())).limit(4).map(Pair::getValue).toList();
        List<EntityExtractionDTO> entityExtractionDTOS = new ArrayList<>();
        for (RelationExtractionDTO relationExtractionDTO : merged) {
            EntityExtractionDTO sourceNode = cypherSchemaDTO.getNode(relationExtractionDTO.getSourceType());
            EntityExtractionDTO targetNode = cypherSchemaDTO.getNode(relationExtractionDTO.getTargetType());
            if (null != sourceNode){
                boolean none = entityExtractionDTOS.stream().noneMatch(
                        entity -> StrUtil.equals(entity.getEntity(), sourceNode.getEntity())
                );
                if (none) {
                    entityExtractionDTOS.add(sourceNode);
                }
            }
            if (null != targetNode){
                boolean none = entityExtractionDTOS.stream().noneMatch(
                        entity -> StrUtil.equals(entity.getEntity(), targetNode.getEntity())
                );
                if (none) {
                    entityExtractionDTOS.add(targetNode);
                }
            }
        }

        return new CypherSchemaDTO(
                entityExtractionDTOS,
                merged
        );
    }

    @Override
    public List<Map<String, Object>> executeCypher(String cypher) {
        List<Record> records = neo4jRepository.executeCypherNative(cypher, null);
        return mapRecords(records);
    }

    @Override
    public CypherSchemaDTO loadGraphSchema() {

        List<RelationExtractionDTO> relationSchema = neo4jRepository.getRelationSchema();
        List<EntityExtractionDTO> entitySchema = neo4jRepository.getNodeSchema();
        return new CypherSchemaDTO(entitySchema, relationSchema);
    }


    @Override
    public Map<String, List<Map<String, Object>>> executeCypher(List<String> cypher) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        for (String c : cypher){
            List<Map<String, Object>> maps = null;
            try {
                maps = executeCypher(c);
            } catch (Exception e) {
                log.info("执行Cypher语句失败，语句：{}，错误信息：{}", c, e.getMessage());
            }
            result.put(c, maps);
        }
        return result;
    }

    @Override
    public void refreshSchemaSegmentVector() {
        loadCypherSchemaIfAbsent();
        if (cypherSchemaDTO == null) {
            log.warn("图谱schema数据为空,不用刷新分词向量...");
            return;
        }
        log.info("开始刷新图谱schema分词向量...");
        nodeRelationVectorService.refreshSchemaSegmentVector(cypherSchemaDTO);
        log.info("图谱schema分词向量刷新完成...");
    }

    private List<Map<String, Object>> mapRecords(List<Record> records) {
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (Record record : records) {
            HashMap<String, Object> map = new HashMap<>();
            for (String key : record.keys()) {
                org.neo4j.driver.Value value = record.get(key);
                String typeName = value.type().name();
                if (typeName.equals("NULL")){
                    map.put(key,null);
                }

                if (StrUtil.equalsAny(typeName, "BOOLEAN","STRING", "NUMBER", "INTEGER", "FLOAT")){
                    // MATCH (n)-[r]-() where n.caseId= '1'  RETURN  n.recordId limit 10
                    map.put(key,value.asObject());
                }

                if (typeName.equals("PATH")){
                    // MATCH p=(n)-[*2]-() where n.caseId= '1'  RETURN  p limit 10
                    map.put(key,new PathDTO(value.asPath()));
                }

                if (typeName.equals("RELATIONSHIP")){
                    // MATCH (n)-[r]-() where n.caseId= '1'  RETURN  r limit 10
                    map.put(key,new RelationshipValueDTO((InternalRelationship) value.asRelationship()));
                }
                if (typeName.equals("LIST OF ANY?")){

                    List<RelationshipValueDTO> list = value.asList().stream()
                            .map(i -> new RelationshipValueDTO((InternalRelationship) i)).toList();
                    map.put(key,list);
                }
                if (typeName.equals("NODE")){
                    // MATCH (n)-[r]-() where n.caseId= '1'  RETURN  r limit 10
                    map.put(key,new NodeDTO((InternalNode) value.asNode()));
                }
                recordList.add(map);
            }
        }
        return recordList;
    }
    /**
     * 加载图谱schema数据，如果不存在则从数据库加载
     * @return
     */
    private void loadCypherSchemaIfAbsent() {
        if (cypherSchemaDTO == null) {
            synchronized (TripleToCypherExecutorImpl.class) {
                if (cypherSchemaDTO == null) {
                    cypherSchemaDTO = this.loadGraphSchema();
                }
            }
        }
    }

    /**
     * 合并节点和关系标签
     * @return
     */
    private List<String> mergeNodeAndRelationLabel() {
        loadCypherSchemaIfAbsent();
        if (CollUtil.isEmpty(cypherSchemaDTO.getRelations())) {
            log.warn("图谱schema数据为空，无法合并节点和关系标签");
            return new ArrayList<>();
        }
        return  cypherSchemaDTO.getRelations().stream()
                .flatMap(r -> Stream.of(r.getSourceType(), r.getRelation(), r.getTargetType())).distinct().collect(Collectors.toList());
    }

    /**
     * 获取关系的schema
     * @return
     */
    public List<RelationExtractionDTO> getRelationSchema() {
        String queryProper = """
                 CALL db.schema.relTypeProperties()
                    YIELD relType, propertyName, propertyTypes
                    RETURN relType, propertyName, propertyTypes
                """;
        Map<String, List<Map<String, String>>> relationProperties = new HashMap<>();
        try (Session session = driver.session()) {
            Result result = session.run(queryProper);
            for (Record record : result.list()) {
                String relType = record.get("relType").asString();
                if (StrUtil.isEmpty(relType)) {
                    continue;
                }
                relType = relType.substring(1, relType.length() - 1).replace("`", "");
                String propertyName = record.get("propertyName").asString();
                List<String> propertyTypes = record.get("propertyTypes").asList(Value::asString);

                List<Map<String, String>> properties = relationProperties.computeIfAbsent(relType, k -> new ArrayList<>());
                boolean noneMatch = properties.stream().noneMatch(
                        prop -> StrUtil.equals(prop.get("propertyName"), propertyName)
                );
                if (noneMatch) {
                    Map<String, String> propMap = new HashMap<>();
                    propMap.put("propertyName", propertyName);
                    propMap.put("propertyTypes", CollUtil.getFirst(propertyTypes));
                    properties.add(propMap);
                }
            }

            List<RelationExtractionDTO> relationExtractionDTOS = new ArrayList<>();
            String queryEndpoints = """
                    MATCH (s)-[r: `{rtype}` ]->(t)
                            WITH labels(s)[0] AS src, labels(t)[0] AS tgt
                            RETURN src, tgt
                    """;
            for (Map.Entry<String, List<Map<String, String>>> entry : relationProperties.entrySet()) {
                String relType = entry.getKey();
                List<Map<String, String>> properties = entry.getValue();
                String formatted = StrUtil.format(queryEndpoints, Map.of("rtype", relType));
                Result run = session.run(formatted);
                for (Record record : run.list()) {
                    String sourceType = record.get("src").asString();
                    String targetType = record.get("tgt").asString();
                    List<ERAttributeDTO> attributeDTOS = properties.stream().map(
                            prop -> new ERAttributeDTO(prop.get("propertyName"), null, prop.get("propertyTypes"))
                    ).collect(Collectors.toList());
                    RelationExtractionDTO relationExtractionDTO = new RelationExtractionDTO( null, sourceType,
                            relType,
                            null,
                            targetType,
                            attributeDTOS);
                    // 合并关系数据
                    Optional<RelationExtractionDTO> optional = relationExtractionDTOS.stream().filter(rel ->
                            StrUtil.equals(rel.getSourceType(), sourceType) &&
                                    StrUtil.equals(rel.getRelation(), relType) &&
                                    StrUtil.equals(rel.getTargetType(), targetType)).findFirst();

                    if (optional.isPresent()) {
                        List<ERAttributeDTO> attributes = optional.get().getAttributes();
                        for (ERAttributeDTO attribute : attributeDTOS) {
                            boolean noneMatch = attributes.stream().noneMatch(
                                    attr -> StrUtil.equals(attr.getAttribute(), attribute.getAttribute())
                            );
                            if (noneMatch) {
                                attributes.add(attribute);
                            }
                        }
                    } else {
                        // 如果不存在，直接添加
                        relationExtractionDTO.setAttributes(attributeDTOS);
                        relationExtractionDTOS.add(relationExtractionDTO);
                    }
                }
            }
            return relationExtractionDTOS;
        }
    }


}
