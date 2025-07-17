package com.xm.text_to_cypher.dto.neo4j;

import lombok.Data;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class PathDTO {

    private List<NodeDTO> nodes;

    private List<RelationshipValueDTO> relationships;

    public PathDTO() {
    }

    public PathDTO(Path path) {
        Iterator<Node> nodeIterator = path.nodes().iterator();
        List<NodeDTO> nodes = new ArrayList<>();
        while (nodeIterator.hasNext()){
            Node next = nodeIterator.next();
            nodes.add(new NodeDTO((InternalNode) next));
        }
        this.nodes = nodes;


        Iterator<Relationship> iterator = path.relationships().iterator();
        List<RelationshipValueDTO> relationships = new ArrayList<>();
        while (iterator.hasNext()){
            relationships.add(new RelationshipValueDTO((InternalRelationship) iterator.next()));
        }
        this.relationships = relationships;
    }
}
