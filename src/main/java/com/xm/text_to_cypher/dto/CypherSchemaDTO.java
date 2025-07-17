package com.xm.text_to_cypher.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CypherSchemaDTO
 */
public class CypherSchemaDTO {

    private final List<EntityExtractionDTO> nodes;

    private final List<RelationExtractionDTO> relations;

    public CypherSchemaDTO(List<EntityExtractionDTO> nodes, List<RelationExtractionDTO> relations) {
        this.nodes = nodes;
        this.relations = relations;
    }

    /**
     * 根据源节点类型或目标节点类型获取关系抽取DTO列表
     * @param str 源节点类型或目标节点类型或关系
     * @return
     */
    public List<RelationExtractionDTO> getRelations(String str) {
        List<RelationExtractionDTO> result = new ArrayList<>();
        for (RelationExtractionDTO relationDTO : relations) {
            if (StrUtil.equals(relationDTO.getSourceType(), str) ||
                StrUtil.equals(relationDTO.getTargetType(), str) ||
                 StrUtil.equals(relationDTO.getRelation(), str)) {

                boolean noneMatch = result.stream().noneMatch(
                        r -> StrUtil.equals(r.getSourceType(), relationDTO.getSourceType()) &&
                                StrUtil.equals(r.getRelation(), relationDTO.getRelation()) &&
                                StrUtil.equals(r.getTargetType(), relationDTO.getTargetType())
                );
                if (noneMatch){
                    result.add(relationDTO);
                }
            }
        }
        return result;
    }

    public List<EntityExtractionDTO> getNodes() {
        return nodes;
    }

    public List<RelationExtractionDTO> getRelations() {
        return relations;
    }

    /**
     * 根据实体名获取关系抽取DTO列表
     * @param entity
     * @return
     */
    public EntityExtractionDTO getNode(String entity) {
        for (EntityExtractionDTO node : nodes) {
            if (StrUtil.equals(node.getEntity(), entity)) {
                return node;
            }
        }
        return null;
    }

    public String format(){
        JSONObject nodeJson = new JSONObject();
        for (EntityExtractionDTO node : nodes) {
            String entity = node.getEntity();
            List<ERAttributeDTO> attributes = node.getAttributes();
            JSONObject nodeAttr = nodeJson.getJSONObject(entity);
            if (nodeAttr == null) {
                nodeAttr = new JSONObject();
                nodeJson.set(entity, nodeAttr);
            }
            for (ERAttributeDTO attribute : attributes) {
                boolean none = nodeAttr.entrySet().stream().noneMatch(
                        entry -> StrUtil.equals(entry.getKey(), attribute.getAttribute()));
                if (none){
                    nodeAttr.set(attribute.getAttribute(), attribute.getDataType());
                }
            }

        }
        JSONObject relJson = new JSONObject();
        for (RelationExtractionDTO relation : relations) {
            String sourceType = relation.getSourceType();
            String targetType = relation.getTargetType();
            String rela = relation.getRelation();
            JSONObject json = relJson.getJSONObject(rela);
            if (null == json) {
                json = new JSONObject();
                relJson.set(rela, json);
            }
            JSONArray endpoints = json.getJSONArray("_endpoints");
            if (null == endpoints){
                endpoints = new JSONArray();
                endpoints.add(Map.of("sourceType", sourceType, "targetType", targetType));
                json.set("_endpoints", endpoints);
            }else {
                boolean absent = false;
                for (Object endpoint : endpoints) {
                    Map<String,Object> nodes = (Map<String, Object>) endpoint;
                    if (sourceType.equals(nodes.get("sourceType"))|| sourceType.equals(nodes.get("targetType"))){
                        absent = true;
                        break;
                    }
                }
                if (absent){
                    endpoints.add(Map.of("sourceType", sourceType, "targetType", targetType));
                }
            }
            for (ERAttributeDTO attribute : relation.getAttributes()) {
                boolean none = json.entrySet().stream().noneMatch(
                        entry -> StrUtil.equals(entry.getKey(), attribute.getAttribute())
                );
                if (none) {
                    json.set(attribute.getAttribute(), attribute.getDataType());
                }
            }
        }
        JSONObject object = new JSONObject()
                .set("nodetypes", nodeJson)
                .set("relationshiptypes", relJson);
        return object.toString();

    }
}
