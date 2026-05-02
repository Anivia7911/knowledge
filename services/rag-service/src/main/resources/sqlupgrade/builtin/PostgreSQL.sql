/*AI模型*/
create table ai_model
(
    id            bigint not null
        primary key,
    create_user   varchar(100),
    create_date   timestamp without time zone,
    update_user   varchar(100),
    update_date   timestamp without time zone,
    deleted       int default 0,
    name          varchar(100),
    provider_type varchar(100),
    enabled       boolean
);
