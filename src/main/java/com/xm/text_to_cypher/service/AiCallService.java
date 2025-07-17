package com.xm.text_to_cypher.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @description: AI调用服务
 */
public interface AiCallService {


    String call(String prompt);

    Flux<ChatResponse> stream(Prompt prompt);

    Embedding embedding(String text);

    List<Embedding> embedding(List<String> texts);
}
