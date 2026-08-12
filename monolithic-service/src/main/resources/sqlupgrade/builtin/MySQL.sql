/*文件头*/
create table if not exists file_header
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date datetime,
    update_user varchar(100),
    update_date datetime,
    deleted     int default 0,
    modular     varchar(100),
    modular_id  varchar(100),
    body_id     bigint,
    name        varchar(100)
) engine=innodb default charset=utf8mb4;
create index file_header_idx1 on file_header (body_id);
create index file_header_idx2 on file_header (modular, modular_id);

/*文件体*/
create table if not exists file_body
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date datetime,
    update_user varchar(100),
    update_date datetime,
    deleted     int default 0,
    size        bigint,
    type        varchar(100),
    scheme      varchar(100),
    version     int,
    md5         varchar(100),
    path        varchar(500)
) engine=innodb default charset=utf8mb4;
/*知识库*/
create table if not exists knowledge_base
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date datetime,
    update_user varchar(100),
    update_date datetime,
    deleted     int default 0,
    name        varchar(200),
    description varchar(1000),
    doc_count   int default 0,
    parent_id   bigint default 0 comment '父知识库ID，0表示顶级'
) engine=innodb default charset=utf8mb4;
create index knowledge_base_idx1 on knowledge_base (parent_id);

/*知识库文档*/
create table if not exists knowledge_document
(
    id                bigint not null
        primary key,
    create_user       varchar(100),
    create_date       datetime,
    update_user       varchar(100),
    update_date       datetime,
    deleted           int default 0,
    knowledge_base_id bigint,
    file_id           bigint,
    file_name         varchar(200),
    file_type         varchar(100),
    status            int default 0,
    progress_current  int default 0 comment '向量化当前进度（已完成分片数）',
    progress_total    int default 0 comment '向量化总分片数'
) engine=innodb default charset=utf8mb4;
create index knowledge_document_idx1 on knowledge_document (knowledge_base_id);
create index knowledge_document_idx2 on knowledge_document (file_id);

-- 已有表补充进度字段（新建表已在上面包含）
alter table knowledge_document add column progress_current int default 0 comment '向量化当前进度（已完成分片数）';
alter table knowledge_document add column progress_total int default 0 comment '向量化总分片数';

/*AI模型*/
create table if not exists ai_model
(
    id            bigint not null
        primary key,
    create_user   varchar(100),
    create_date   datetime,
    update_user   varchar(100),
    update_date   datetime,
    deleted       int default 0,
    name          varchar(100),
    provider_type varchar(100),
    api_key       varchar(500),
    api_url       varchar(500),
    model_type    varchar(100),
    model_name    varchar(200),
    enabled       tinyint(1),
    default_model tinyint(1)
) engine=innodb default charset=utf8mb4;

/*AI对话会话*/
create table if not exists chat_conversation
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date datetime,
    update_user varchar(100),
    update_date datetime,
    deleted     int default 0,
    title       varchar(500),
    model_id    bigint,
    knowledge_base_ids varchar(500) default '' comment '会话使用的知识库ID，逗号分隔'
) engine=innodb default charset=utf8mb4;

/*AI对话消息*/
create table if not exists chat_message
(
    id              bigint not null
        primary key,
    create_user     varchar(100),
    create_date     datetime,
    update_user     varchar(100),
    update_date     datetime,
    deleted         int default 0,
    conversation_id bigint,
    role            varchar(50),
    content         text
) engine=innodb default charset=utf8mb4;
create index chat_message_idx1 on chat_message (conversation_id);
