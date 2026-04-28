create table knowledge_test_table
(
    id_          integer not null
        primary key,
    create_user_ varchar(50),
    create_date_ timestamp without time zone,
    update_user_ varchar(50),
    update_date_ timestamp without time zone
);