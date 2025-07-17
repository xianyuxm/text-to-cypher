package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xm.text_to_cypher.constant.PromptConst;
import com.xm.text_to_cypher.service.AiCallService;
import com.xm.text_to_cypher.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import static com.xm.text_to_cypher.constant.PromptConst.GENERATE_ANSWER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final GeneralRetriever retriever;

    private final AiCallService aiCallService;

    @Override
    public Flux<String> streamChat(String query) {

        log.info("用户查询: {}", query);
        List<Map<String, Object>> graphResult = retriever.retrieval(query);
        if (CollUtil.isEmpty(graphResult)){
            log.info("没有找到匹配的schema，query: {}", query);
            return Flux.just("查无结果").concatWith(Flux.just("[END]"));
        }
        //生成回答
        SystemPromptTemplate generateAnswerTemplate = new SystemPromptTemplate(PromptConst.promptMap.get(GENERATE_ANSWER));
        Message message = generateAnswerTemplate.createMessage(Map.of("reference_data", JSONUtil.toJsonStr(graphResult), "query", query));
        log.info("生成回答的提示词：{}", message);
        return aiCallService.stream(new Prompt(message))
                .map(response -> response.getResult().getOutput().getText())
                .concatWith(Flux.just("[END]"));
    }

}
