package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.text_to_cypher.dto.EntityExtractionDTO;
import com.xm.text_to_cypher.dto.RelationExtractionDTO;
import com.xm.text_to_cypher.dto.ERAttributeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
@RequiredArgsConstructor
public class Neo4jRepository implements com.xm.text_to_cypher.service.Neo4jRepository {
    /**
     * Neo4j 驱动
     */
    private final Driver driver;



    /**
     * 执行原生 Cypher 语句
     * @param cypher 原生 Cypher 语句
     * @param params 参数
     * @return List<Record>
     */
    public List<Record> executeCypherNative(String cypher, Map<String, Object> params) {
        try (Session session = driver.session()) {
            Result run = session.run(cypher, params == null ? Collections.emptyMap() : params);
            return run.list();
        }
    }


    /**
     * 获取节点的schema
     * @return
     */
    public List<EntityExtractionDTO> getNodeSchema(){

        String query = """
                CALL db.schema.nodeTypeProperties()
                    YIELD nodeType, propertyName, propertyTypes
                    RETURN nodeType, propertyName, propertyTypes
                """;
        try (Session session = driver.session()) {

            List<EntityExtractionDTO> extractionDTOS = new ArrayList<>();
            Result result = session.run(query);
            for (Record record : result.list()) {
                String nodeType = record.get("nodeType").asString();
                if (StrUtil.isEmpty(nodeType)){
                    continue;
                }
                nodeType = nodeType.substring(1, nodeType.length()-1).replace("`", "");
                String propertyName = record.get("propertyName").asString();
                List<String> propertyTypes = record.get("propertyTypes").asList(Value::asString);

                // 创建属性DTO
                ERAttributeDTO attributeDTO = new ERAttributeDTO(propertyName, null, CollUtil.getFirst(propertyTypes));

                // 检查是否已存在该节点类型
                final String nodeType_f = nodeType;
                EntityExtractionDTO existingEntity = extractionDTOS.stream()
                        .filter(e -> StrUtil.equals(e.getEntity(), nodeType_f))
                        .findFirst().orElse(null);

                if (existingEntity != null) {
                    // 如果已存在，添加属性
                    List<ERAttributeDTO> attributes = existingEntity.getAttributes();
                    boolean noneMatch = attributes.stream().noneMatch(
                            attr -> StrUtil.equals(attr.getAttribute(), attributeDTO.getAttribute())
                    );
                    if (noneMatch) {
                        // 如果属性不存在，添加属性
                        attributes.add(attributeDTO);
                    }
                } else {
                    // 如果不存在，创建新的实体DTO
                    List<ERAttributeDTO> ERAttributeDTOS = new ArrayList<>();
                    ERAttributeDTOS.add(attributeDTO);
                    EntityExtractionDTO entityExtractionDTO = new EntityExtractionDTO(nodeType,  null, ERAttributeDTOS);
                    extractionDTOS.add(entityExtractionDTO);
                }
            }
            return extractionDTOS;
        }
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
