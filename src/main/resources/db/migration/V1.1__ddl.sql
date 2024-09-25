CREATE TABLE "user" (
    id serial primary key,
    name varchar(150) not null,
    email varchar(200) not null,
    password varchar(150) not null
);