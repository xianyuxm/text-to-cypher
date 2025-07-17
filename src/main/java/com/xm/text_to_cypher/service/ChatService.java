package com.xm.text_to_cypher.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 知识问答
     *
     * @param userQuery 用户查询
     * @return 知识问答结果
     */
    Flux<String> streamChat(String userQuery);
}
