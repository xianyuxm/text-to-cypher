package com.xm.text_to_cypher.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.xm.text_to_cypher.constant.PromptConst;
import com.xm.text_to_cypher.dto.CypherSchemaDTO;
import com.xm.text_to_cypher.service.AiCallService;
import com.xm.text_to_cypher.service.Retriever;
import com.xm.text_to_cypher.service.TripleToCypherExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.xm.text_to_cypher.constant.PromptConst.*;


@Slf4j
@Service("dataCompareRetriever")
@RequiredArgsConstructor
public class GeneralRetriever implements Retriever {

    private final TripleToCypherExecutor tripleToCypherExecutor;

    private final AiCallService aiCallService;

    @Override
    public List<Map<String, Object>> retrieval(String query) {
        log.info("retrieval: 执行数据对比检索器，查询内容：{}", query);
        if (StrUtil.isEmpty(query)) {
            log.warn("查询内容为空，无法执行数据对比检索");
            return new ArrayList<>();
        }
        // 查询问题可能涉及到的关系图谱schema
        CypherSchemaDTO schemaDTO = tripleToCypherExecutor.queryRelationSchema(query);
        log.info("retrieval: 查询到的关系图谱schema 节点个数：{} ,关系结束{} ", schemaDTO.getNodes().size(), schemaDTO.getRelations().size());
        log.info("retrieval: 查询到的关系图谱schema ：{} ", schemaDTO.format());
        if (CollUtil.isEmpty(schemaDTO.getRelations()) || CollUtil.isEmpty(schemaDTO.getNodes())) {
            log.info("没有找到匹配的关系或实体，query: {}", query);
            return new ArrayList<>();
        }
        // 利用大模型生成可执行的cypher语句
        String prompt = PromptConst.promptMap.get(TEXT_TO_CYPHER);
        String format = StrUtil.format(prompt, Map.of("query", query, "schema", schemaDTO.format(), "env", "- 当前时间是:" + DateUtil.now()));
        log.info("retrieval: 生成的cypher语句：{}", format);
        String call = aiCallService.call(format);
        log.info("retrieval: AI调用返回结果：{}", call);
        if (StrUtil.isEmpty(call)) {
            log.warn("retrieval: AI调用返回结果为空，无法执行Cypher查询");
            return new ArrayList<>();
        }
        List<Map<String, Object>> result =  new ArrayList<>();
        JSONArray js = JSONUtil.parseArray(call);
        Map<String, List<Map<String, Object>>> cypherData = tripleToCypherExecutor.executeCypher(js.toList(String.class));
        if (CollUtil.isNotEmpty(cypherData)) {
            boolean allEmpty = cypherData.values().stream().noneMatch(CollUtil::isNotEmpty);
            if (!allEmpty){
                cypherData.values().stream().filter(CollUtil::isNotEmpty).forEach(result::addAll);
                return result;
            }
        }
        if (CollUtil.isEmpty(result)){
            log.info("retrieval: 执行Cypher语句无结果，重新调整cypher语句：{}", query);
            prompt = PromptConst.promptMap.get(TEXT_TO_CYPHER_RETRY);
            format = StrUtil.format(prompt,
                    Map.of("query", query, "schema", schemaDTO.format(),
                            "env", "- 当前时间是:" + DateUtil.now(),"cypher",js.toString()));
            log.info("retrieval: 生成cypher的语句：{}", format);
            call = aiCallService.call(format);
            log.info("retrieval: AI调用返回结果：{}", call);

            js = JSONUtil.parseArray(call);
            cypherData = tripleToCypherExecutor.executeCypher(js.toList(String.class));
            if (CollUtil.isNotEmpty(cypherData)) {
                boolean allEmpty2 = cypherData.values().stream().noneMatch(CollUtil::isNotEmpty);
                if (!allEmpty2){
                    cypherData.values().stream().filter(CollUtil::isNotEmpty).forEach(result::addAll);
                    return result;
                }
            }
        }

        return result;
    }
}
