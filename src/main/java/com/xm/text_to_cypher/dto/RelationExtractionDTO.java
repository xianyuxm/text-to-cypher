package com.xm.text_to_cypher.dto;

import lombok.Data;
import java.util.List;

/**
 * 关系抽取
 */
@Data
public class RelationExtractionDTO {

    private String id;

    /**
     * 头节点数据
     */
    private String source;

    /**
     * 头节点类型
     */
    private String sourceType;

    private String sourceTypeEn;

    /**
     *关系
     */
    private String relation;

    private String relationEn;

    /**
     * 尾节点数据
     */
    private String target;

    /**
     * 尾节点类型
     */
    private String targetType;

    private String targetTypeEn;

    private List<ERAttributeDTO> attributes;

    public RelationExtractionDTO() {
    }


    public RelationExtractionDTO(String source, String sourceType, String relation, String target, String targetType, List<ERAttributeDTO> attributes) {
        this.source = source;
        this.relation = relation;
        this.target = target;
        this.attributes = attributes;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }


}
