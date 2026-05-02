/*AI模型*/
create table ai_model
(
    id            bigint not null
        primary key,
    create_user   varchar(100) null,
    create_date   datetime null,
    update_user   varchar(100) null,
    update_date   datetime null,
    deleted       int default 0,
    name          varchar(100) null,
    provider_type varchar(100) null,
    enabled       boolean null
);