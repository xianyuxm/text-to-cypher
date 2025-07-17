package com.xm.text_to_cypher.service;


import com.xm.text_to_cypher.dto.TextTerm;

import java.util.List;

public interface TextToSegmentService {

    /**
     * 对文本进行分词
     * @param text 需要分词的文本
     * @return 分词结果列表
     */
    List<TextTerm> segmentText(String text);

    /**
     * 添加自定义词典 覆盖模式，如果词典中已存在该词，则更新其标签和频率
     * @param word 需要添加的词
     * @param label 词的标签
     * @param frequency 词的频率 数值越大，优先级越高
     */
    void addDict(String word, String label,int frequency);
}
