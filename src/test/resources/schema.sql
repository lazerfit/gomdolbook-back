create table IF NOT EXISTS users (
                    user_id bigint auto_increment primary key,
                    email varchar(255) not null,
                    picture varchar(255) not null,
                    role varchar(20)
);

create table user_collection
(
    user_collection_id bigint auto_increment
        primary key,
    user_id            bigint       null,
    name               varchar(255) not null,
    constraint FK6o8039xka2t1u0mqvi3vfci8r
        foreign key (user_id) references users (user_id)
);

create table IF NOT EXISTS reading_log
(
    readinglog_id bigint auto_increment
        primary key,
    note1         varchar(255)                            null,
    note2         varchar(255)                            null,
    note3         varchar(255)                            null,
    status        enum ('FINISHED', 'READING', 'TO_READ', 'NEW') null,
    user_id       bigint                                  null,
    constraint FKREADINGLOGUSERS
        foreign key (user_id) references users (user_id)
);

create table book
(
    book_id       bigint auto_increment
        primary key,
    readinglog_id bigint       null,
    author        varchar(255) null,
    category_name varchar(255) null,
    cover         varchar(255) null,
    description   varchar(255) null,
    isbn13        varchar(255) null,
    pub_date      varchar(255) null,
    publisher     varchar(255) null,
    title         varchar(255) null,
    constraint UKgqjxn93jsf20p2nsa9o1g802
        unique (readinglog_id),
    constraint FKbrvddxu6yc8myt92lhmltdvco
        foreign key (readinglog_id) references reading_log (readinglog_id)
);

create table book_user_collection
(
    book_id                 bigint null,
    book_user_collection_id bigint auto_increment
        primary key,
    user_collection_id      bigint null,
    user_id                 bigint null,
    constraint FK2tbxumgb4slms3tsq1a3bxx6g
        foreign key (user_collection_id) references user_collection (user_collection_id),
    constraint FKgpv32t2rb0523idwkig0vb3nx
        foreign key (book_id) references book (book_id),
    constraint FKlmun6tk80jyg0toe64864jh9i
        foreign key (user_id) references users (user_id)
);



