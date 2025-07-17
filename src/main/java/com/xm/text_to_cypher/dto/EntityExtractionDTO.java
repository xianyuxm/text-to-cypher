package com.xm.text_to_cypher.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体抽取
 */
@Data
public class EntityExtractionDTO {

    private String id;

    /**
     * 实体标签
     */
    private String entity;

    /**
     * 实体英文名
     */
    private String entityEn;

    /**
     * 实体名
     */
    private String name;

    private List<ERAttributeDTO> attributes = new ArrayList<>();

    public EntityExtractionDTO() {
    }


    public EntityExtractionDTO(String entity, String name, List<ERAttributeDTO> attributes) {
        this.entity = entity;
        this.name = name;
        this.attributes = attributes;
    }
}
