package com.xm.text_to_cypher.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 实体关系向量表
 * @TableName entity_relation_vector
 */
@TableName(value ="entity_relation_vector")
@Data
public class EntityRelationVector implements Serializable {
    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 向量值
     */
    private Object embedding;

    /**
     * 内容类型 E:节点 R:关系 ERE:三元组
     */
    private String contentType;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}