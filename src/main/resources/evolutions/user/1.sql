# --- !Ups

create table "user"
(
    login VARCHAR(32) not null
        constraint user_pk
            primary key,
    password VARCHAR(32) not null
);



# --- !Downs

DROP TABLE "user";
