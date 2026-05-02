/*文件头*/
create table file_header
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date timestamp without time zone,
    update_user varchar(100),
    update_date timestamp without time zone,
    deleted     int default 0,
    modular     varchar(100),
    modular_id  varchar(100),
    body_id     bigint,
    name        varchar(100)
);
create index file_header_idx1 on file_header (body_id);
create index file_header_idx2 on file_header (modular, modular_id);

/*文件体*/
create table file_body
(
    id          bigint not null
        primary key,
    create_user varchar(100),
    create_date timestamp without time zone,
    update_user varchar(100),
    update_date timestamp without time zone,
    deleted     int default 0,
    size        bigint,
    type        varchar(100),
    scheme      varchar(100),
    version     integer
);

alter table file_body
    add column md5 varchar(100);