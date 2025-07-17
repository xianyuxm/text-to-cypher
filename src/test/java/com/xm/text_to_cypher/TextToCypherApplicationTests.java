package com.xm.text_to_cypher;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.json.JSONUtil;
import com.xm.text_to_cypher.service.Retriever;
import com.xm.text_to_cypher.service.TextToSegmentService;
import com.xm.text_to_cypher.service.TripleToCypherExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class TextToCypherApplicationTests {

    @Autowired
    private TripleToCypherExecutor tripleToCypherExecutor;

    @Autowired
    private TextToSegmentService textToSegmentService;

    @Autowired
    private Retriever retriever;

    @Test
    public void queryTest() {

        // 刷新neo4j schema
        tripleToCypherExecutor.refreshSchemaSegmentVector();


        TimeInterval timer = new TimeInterval();
        // 可以自定义分词字典
        textToSegmentService.addDict("软件工程师","职业",1000);

        // 查询问题
        List<Map<String, Object>> retrieval = retriever.retrieval("张三的职业是什么");
        log.info(JSONUtil.toJsonStr(retrieval));
        log.info("<<<===========================>>> 耗时: {} 毫秒", timer.intervalMs());
    }

}
