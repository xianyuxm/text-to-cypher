package com.xm.text_to_cypher.dto.neo4j;

import lombok.Data;
import org.neo4j.driver.internal.InternalRelationship;

import java.util.Map;

@Data
public class RelationshipValueDTO {


    private Long start;

    private String startElementId;

    private Long end;

    private String endElementId;

    private String type;

    private Long id;

    private String elementId;

    private Map<String,Object> properties;


    public RelationshipValueDTO() {
    }

    public RelationshipValueDTO(InternalRelationship relationship) {
        this.start = relationship.startNodeId();
        this.startElementId = relationship.startNodeElementId();
        this.end = relationship.endNodeId();
        this.endElementId = relationship.endNodeElementId();
        this.type = relationship.type();
        this.id = relationship.id();
        this.elementId = relationship.elementId();
        this.properties = relationship.asMap();

    }


    public void clearGraphElement() {
        this.id = null;
        this.elementId = null;
        this.start = null;
        this.startElementId = null;
        this.end = null;
        this.endElementId = null;
    }
}
