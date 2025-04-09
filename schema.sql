create table book (book_id bigint generated by default as identity, readinglog_id bigint unique, author varchar(255), category_name varchar(255), cover varchar(255), description varchar(255), isbn varchar(255), pub_date varchar(255), publisher varchar(255), title varchar(255), primary key (book_id));
create table book_collection (book_collection_id bigint generated by default as identity, book_id bigint, collection_id bigint, user_id bigint, primary key (book_collection_id));
create table collection (collection_id bigint generated by default as identity, user_id bigint, name varchar(255) not null unique, primary key (collection_id));
create table reading_log (rating integer, readinglog_id bigint generated by default as identity, user_id bigint, note1 TEXT, note2 TEXT, note3 TEXT, status enum ('FINISHED','NEW','READING','TO_READ'), primary key (readinglog_id));
create table users (user_id bigint generated by default as identity, email varchar(255) not null unique, picture varchar(255) not null, role enum ('ADMIN','USER'), primary key (user_id));
alter table if exists book add constraint FKbrvddxu6yc8myt92lhmltdvco foreign key (readinglog_id) references reading_log;
alter table if exists book_collection add constraint FKhrhume0ucplaek9m8pb6ild7s foreign key (book_id) references book;
alter table if exists book_collection add constraint FKka9jqqmcu25by7m32gihxb4rr foreign key (collection_id) references collection;
alter table if exists book_collection add constraint FKo3e3fm34hbvwbcft0p856dmah foreign key (user_id) references users;
alter table if exists collection add constraint FK45y8o0xk4kptog8w86usq3yjl foreign key (user_id) references users;
alter table if exists reading_log add constraint FKbecasd79ecm9vvexa47hwfepy foreign key (user_id) references users;
