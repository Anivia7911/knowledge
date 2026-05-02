/*文件头*/
create table file_header
(
    id_          bigint not null
        primary key,
    create_user_ varchar(100) null,
    create_date_ datetime null,
    update_user_ varchar(100) null,
    update_date_ datetime null,
    modular_     varchar(100) null,
    modular_id_  varchar(100) null,
    body_id_     bigint null,
    name_        varchar(100) null
);
create index file_header_idx1 on file_header (body_id_);
create index file_header_idx2 on file_header (modular_, modular_id_);

/*文件体*/
create table file_body
(
    id_          bigint not null
        primary key,
    create_user_ varchar(100) null,
    create_date_ datetime null,
    update_user_ varchar(100) null,
    update_date_ datetime null,
    size_        bigint null,
    type_        varchar(100) null,
    scheme_      varchar(100) null,
    version_     integer null
);

alter table file_body
    add column md5 varchar(100);