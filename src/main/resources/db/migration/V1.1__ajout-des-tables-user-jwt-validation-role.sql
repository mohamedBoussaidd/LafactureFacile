
CREATE TABLE "roles"(
    id serial primary key,
    name varchar(20) not null
);
CREATE TABLE "user" (
    id serial primary key,
    name varchar(150) not null,
    email varchar(200) not null,
    password varchar(150) not null,
    id_Activation varchar(100) not null,
    actif BOOLEAN
);
CREATE TABLE "jwt"(
    id serial primary key,
    value varchar(150) not null,
    is_blacklisted BOOLEAN,
    is_expired BOOLEAN,
    user_id integer not null,
    foreign key (user_id) references "user"(id)
);

CREATE TABLE "validations"(
    id serial primary key,
    creation TIMESTAMP not null,
    expired TIMESTAMP not null,
    activation TIMESTAMP,
    code varchar(100) not null,
    user_id integer not null,
    foreign key (user_id) references "user"(id)
);
CREATE Table "user_roles"(
    id serial primary key,
    user_id integer not null,
    roles_id integer not null,
    foreign key (user_id) references "user"(id),
    foreign key (roles_id) references "roles"(id)
);