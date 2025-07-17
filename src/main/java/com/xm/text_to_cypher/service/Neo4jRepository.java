package com.xm.text_to_cypher.service;

import com.xm.text_to_cypher.dto.EntityExtractionDTO;
import com.xm.text_to_cypher.dto.RelationExtractionDTO;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Map;

public interface Neo4jRepository {

    List<Record> executeCypherNative(String cypher, Map<String, Object> params);

    List<EntityExtractionDTO> getNodeSchema();

    List<RelationExtractionDTO> getRelationSchema();
}
