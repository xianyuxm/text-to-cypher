package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.text_to_cypher.domain.EntityRelationVector;
import com.xm.text_to_cypher.dto.CypherSchemaDTO;
import com.xm.text_to_cypher.dto.EntityExtractionDTO;
import com.xm.text_to_cypher.dto.RelationExtractionDTO;
import com.xm.text_to_cypher.service.AiCallService;
import com.xm.text_to_cypher.service.EntityRelationVectorService;
import com.xm.text_to_cypher.mapper.EntityRelationVectorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
* @author Administrator
* @description 针对表【entity_relation_vector(实体关系向量表)】的数据库操作Service实现
* @createDate 2025-07-16 13:28:07
*/
@Service
@RequiredArgsConstructor
public class EntityRelationVectorServiceImpl extends ServiceImpl<EntityRelationVectorMapper, EntityRelationVector>
    implements EntityRelationVectorService{


    private final AiCallService aiCallService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshSchemaSegmentVector(CypherSchemaDTO cypherSchemaDTO) {

        // 删除旧的向量数据
        super.lambdaUpdate().remove();
        // 重新插入新的向量数据
        List<EntityExtractionDTO> nodes = cypherSchemaDTO.getNodes();
        List<RelationExtractionDTO> relations = cypherSchemaDTO.getRelations();
        List<EntityRelationVector> allRelationVectors = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        for (List<RelationExtractionDTO> relationSplit : CollUtil.split(relations, 200)) {
            List<String> rs = relationSplit.stream().map(RelationExtractionDTO::getRelation).toList();
            List<Embedding> embedding = aiCallService.embedding(rs);
            for (Embedding embed : embedding) {
                if (texts.contains(rs.get(embed.getIndex()))){
                    continue;
                }
                texts.add(rs.get(embed.getIndex()));
                EntityRelationVector vector = new EntityRelationVector();
                vector.setContent(rs.get(embed.getIndex()));
                vector.setEmbedding(embed.getOutput());
                vector.setContentType("R");// 关系
                allRelationVectors.add(vector);
            }
            List<String> ers = relationSplit.stream()
                    .map(r -> StrUtil.join(" ", r.getSourceType(), r.getRelation(),r.getTargetType())).toList();
            List<Embedding> erEmbeddings = aiCallService.embedding(ers);
            for (Embedding embed : erEmbeddings) {
                if (texts.contains(ers.get(embed.getIndex()))) {
                    continue;
                }
                texts.add(ers.get(embed.getIndex()));
                EntityRelationVector vector = new EntityRelationVector();
                vector.setContent(ers.get(embed.getIndex()));
                vector.setEmbedding(embed.getOutput());
                vector.setContentType("ERE");// 实体关系
                allRelationVectors.add(vector);
            }
        }
        super.saveBatch(allRelationVectors);
        List<EntityRelationVector> allNodeVectors = new ArrayList<>();
        texts = new ArrayList<>();
        for (List<EntityExtractionDTO> entitySplit : CollUtil.split(nodes, 200)) {
            List<String> es = entitySplit.stream().map(EntityExtractionDTO::getEntity).toList();
            List<Embedding> embedding = aiCallService.embedding(es);
            for (Embedding embed : embedding) {
                if (texts.contains(es.get(embed.getIndex()))) {
                    continue;
                }
                texts.add(es.get(embed.getIndex()));
                EntityRelationVector vector = new EntityRelationVector();
                vector.setContent(es.get(embed.getIndex()));
                vector.setEmbedding(embed.getOutput());
                vector.setContentType("E");// 实体
                allNodeVectors.add(vector);
            }
        }
        super.saveBatch(allNodeVectors);
    }

    @Override
    public List<EntityRelationVector> matchSimilarByCosine(float[] embedding, double threshold, List<String> contentType, int limit) {
        return super.getBaseMapper().findSimilarByCosine(embedding, threshold, contentType, limit);
    }

    @Override
    public Double matchContentScore(float[] embedding, String content) {
        if (StrUtil.isEmpty(content) || embedding == null || embedding.length == 0) {
            return 0.0;
        }
        return super.getBaseMapper().matchContentScore(embedding, content);
    }
}




