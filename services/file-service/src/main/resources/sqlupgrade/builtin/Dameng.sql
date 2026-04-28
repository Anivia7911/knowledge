create table file_header
(
    id_          int not null
        primary key,
    create_user_ varchar(50) null,
    create_date_ datetime null,
    update_user_ varchar(50) null,
    update_date_ datetime,
    modular_     varchar(50) null,
    modular_id_  varchar(50) null,
    body_id_     varchar(50) null,
    name_        varchar(100) null
);

create table file_header
(
    id_          int not null
        primary key,
    create_user_ varchar(50) null,
    create_date_ datetime null,
    update_user_ varchar(50) null,
    update_date_ datetime,
    size_        bigint null,
    type_        varchar(50) null,
    scheme_      varchar(50) null,
    version_     integer null
);