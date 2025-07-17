--  创建pgvector扩展
CREATE EXTENSION vector;

-- 实体关系向量表
create table entity_relation_vector
(
    id           varchar(64) not null
        constraint node_relation_vector_pk
        primary key,
    content      text,
    embedding    VECTOR(1024),
    content_type varchar(64),
    create_time  timestamp default CURRENT_TIMESTAMP,
    update_time  timestamp default CURRENT_TIMESTAMP
);

comment on table entity_relation_vector is '实体关系向量表';
comment on column entity_relation_vector.id is '主键';
comment on column entity_relation_vector.content is '文本内容';
comment on column entity_relation_vector.embedding is '向量值';
comment on column entity_relation_vector.content_type is '内容类型 E:节点 R:关系 ERE:三元组';