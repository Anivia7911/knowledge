create table file_header
(
    id_          int not null
        primary key,
    create_user_ varchar(50) null,
    create_date_ datetime null,
    update_user_ varchar(50) null,
    update_date_ datetime null,
    modular_     varchar(50) null,
    modular_id_  varchar(50) null,
    body_id_     varchar(50) null,
    name_        varchar(100) null
);
create index file_header_idx1 on file_header (body_id_);
create index file_header_idx2 on file_header (modular_, modular_id_);

create table file_body
(
    id_          int not null
        primary key,
    create_user_ varchar(50) null,
    create_date_ datetime null,
    update_user_ varchar(50) null,
    update_date_ datetime null,
    size_        bigint null,
    type_        varchar(50) null,
    scheme_      varchar(50) null,
    version_     integer null
);
