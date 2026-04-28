create table file_header
(
    id_          integer not null
        primary key,
    create_user_ varchar(50),
    create_date_ timestamp without time zone,
    update_user_ varchar(50),
    update_date_ timestamp without time zone,
    modular_     varchar(50),
    modular_id_  varchar(50),
    body_id_     varchar(50),
    name_        varchar(100)
);

create table file_body
(
    id_          integer not null
        primary key,
    create_user_ varchar(50),
    create_date_ timestamp without time zone,
    update_user_ varchar(50),
    update_date_ timestamp without time zone,
    size_        bigint,
    type_        varchar(50),
    scheme_      varchar(50),
    version_     integer
);