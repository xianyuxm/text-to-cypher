package com.xm.text_to_cypher.service;

import com.xm.text_to_cypher.dto.CypherSchemaDTO;

import java.util.List;
import java.util.Map;

/**
 * 三元组转换为Cypher语句的执行器
 */
public interface TripleToCypherExecutor {


    /**
     * 查询关系图谱的schema
     * @param query 用户查询语句
     * @return schema
     */
    CypherSchemaDTO queryRelationSchema(String query);

    CypherSchemaDTO loadGraphSchema();

    /**
     * 执行Cypher语句
     * @param cypher
     * @return
     */
    List<Map<String, Object>> executeCypher(String cypher);

    /**
     * 执行Cypher语句
     * @param cypher
     * @return
     */
    Map<String, List<Map<String, Object>>> executeCypher(List<String> cypher);


    void refreshSchemaSegmentVector();

}
