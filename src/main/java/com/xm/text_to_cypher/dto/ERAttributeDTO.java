package com.xm.text_to_cypher.dto;

import lombok.Data;

/**
 * 实体属性
 */
@Data
public class ERAttributeDTO {

    private String id;

    /**
     * 片段实体属性表 既可以是truncation_entity_extraction表id也可以是truncation_relation_extraction表id
     */
    private String terId;

    /**
     * 类型 0：terId关联的id为实体 1：terId关联的id为关系
     */
    private String associationType;

    /**
     * 属性名
     */
    private String attribute;

    private String attributeEn;

    /**
     * 属性值
     */
    private String value;

    /**
     * 数据类型 0：字符串 1：数字
     */
    private String dataType;

    public ERAttributeDTO() {
    }


    public ERAttributeDTO(String attribute, String value, String dataType) {
        this.attribute = attribute;
        this.value = value;
        this.dataType = dataType;
    }
}
