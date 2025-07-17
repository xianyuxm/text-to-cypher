package com.xm.text_to_cypher.dto.neo4j;

import lombok.Data;
import org.neo4j.driver.internal.InternalNode;

import java.util.Collection;
import java.util.Map;

@Data
public class NodeDTO {

    private Long id;

    private String elementId;

    private Map<String, Object> properties;

    private Collection<String> labels;


    public NodeDTO() {
    }

    public NodeDTO(InternalNode internalNode) {
        this.id = internalNode.id();
        this.elementId = internalNode.elementId();
        this.properties = internalNode.asMap();
        this.labels = internalNode.labels();
    }

    public void clearGraphElement(){
        this.id = null;
        this.elementId = null;
    }
}
