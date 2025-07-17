package com.xm.text_to_cypher.service;

import java.util.List;
import java.util.Map;

/**
 * 检索器接口
 */
public interface Retriever {

    /**
     * 检索数据
     * @param query 问题
     * @return 结果数据
     */
    List<Map<String, Object>> retrieval(String query);

}
