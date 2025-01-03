create table reading_log
(
    readinglog_id bigint auto_increment
        primary key,
    note1         varchar(255)                            null,
    note2         varchar(255)                            null,
    note3         varchar(255)                            null,
    status        enum ('FINISHED', 'READING', 'TO_READ') null
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
    constraint UK_READING_LOG
        unique (readinglog_id),
    constraint FK_BOOK_READING_LOG
        foreign key (readinglog_id) references reading_log (readinglog_id)
);

