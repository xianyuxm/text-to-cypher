package com.xm.text_to_cypher.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KeywordSynonymDTO {

    /**
     *  词语
     */
    private String term;

    /**
     * 词频
     */
    private Integer frequency;

    /**
     * 词性
     */
    private String nature;

    private List<KeywordSynonymDTO> synonyms = new ArrayList<>();
}
