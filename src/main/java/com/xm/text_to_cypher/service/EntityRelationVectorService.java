package com.xm.text_to_cypher.service;

import com.xm.text_to_cypher.domain.EntityRelationVector;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.text_to_cypher.dto.CypherSchemaDTO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【entity_relation_vector(实体关系向量表)】的数据库操作Service
* @createDate 2025-07-16 13:28:07
*/
public interface EntityRelationVectorService extends IService<EntityRelationVector> {

    void refreshSchemaSegmentVector(CypherSchemaDTO cypherSchemaDTO);

    List<EntityRelationVector> matchSimilarByCosine(float[] embedding, double threshold , List<String> contentType, int limit);

    /**
     * 计算内容匹配分数
     * @param embedding 向量
     * @param content 内容
     * @return
     */
    Double matchContentScore(float[] embedding, String content);
}
