create table file_header
(
    id_          integer not null
        primary key,
    modular_     varchar(50),
    modular_id_  varchar(50),
    body_id_     varchar(50),
    name_        varchar(50),
    create_user_ varchar(50),
    create_date_ timestamp without time zone,
    update_user_ varchar(50),
    update_date_ timestamp without time zone
);