package com.xm.text_to_cypher.mapper;

import com.xm.text_to_cypher.domain.EntityRelationVector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Administrator
* @description 针对表【entity_relation_vector(实体关系向量表)】的数据库操作Mapper
* @createDate 2025-07-16 13:28:07
* @Entity com.xm.text_to_cypher.domain.EntityRelationVector
*/
public interface EntityRelationVectorMapper extends BaseMapper<EntityRelationVector> {

    List<EntityRelationVector> findSimilarByCosine(float[] embedding, double threshold, List<String> contentType, int limit);

    Double matchContentScore(float[] embedding, String content);
}




