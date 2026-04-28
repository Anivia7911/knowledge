create table knowledge_test_table
(
    id_          int not null
        primary key,
    create_user_ varchar(50) null,
    create_date_ datetime null,
    update_user_ varchar(50) null,
    update_date_ datetime null
);