package com.xm.text_to_cypher.service.impl;

import com.xm.text_to_cypher.service.AiCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaCallServiceImpl implements AiCallService {

    private final OllamaChatModel ollamaChatModel;

    private final OllamaEmbeddingModel embeddingModel;
    @Override
    public String call(String prompt) {

        return ollamaChatModel.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return ollamaChatModel.stream(prompt);
    }

    public Embedding embedding(String text) {

        EmbeddingResponse embeddingResponse = embeddingModel.call(new EmbeddingRequest(List.of(text),null));
        return embeddingResponse.getResult();
    }

    @Override
    public List<Embedding> embedding(List<String> texts) {
        EmbeddingResponse embeddingResponse = embeddingModel.call(new EmbeddingRequest(texts,null));
        return embeddingResponse.getResults();
    }
}
