
CREATE TABLE IF NOT EXISTS "roles"(
    id serial primary key,
    name varchar(20) not null
);
CREATE TABLE IF NOT EXISTS "user_entity" (
    id serial primary key,
    name varchar(150) not null,
    email varchar(200) not null,
    password varchar(150) not null,
    actif BOOLEAN,
    adresse VARCHAR(255),
    siret VARCHAR(255),
    telephone VARCHAR(20),
    firstname VARCHAR(255),
    city VARCHAR(255),
    postalcode VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS "jwt"(
    id serial primary key,
    value TEXT not null,
    is_blacklisted BOOLEAN,
    is_expired BOOLEAN,
    user_entity_id integer not null,
    foreign key (user_entity_id) references "user_entity"(id)
);

CREATE TABLE IF NOT EXISTS "validations"(
    id serial primary key,
    creation TIMESTAMP not null,
    expired TIMESTAMP not null,
    activation TIMESTAMP,
    code varchar(100) not null,
    uid VARCHAR(50) not null,
    user_entity_id integer not null,
    foreign key (user_entity_id) references "user_entity"(id)
);
CREATE Table IF NOT EXISTS "user_roles"(
    id serial primary key,
    user_entity_id integer not null,
    roles_id integer not null,
    foreign key (user_entity_id) references "user_entity"(id),
    foreign key (roles_id) references "roles"(id)
);
INSERT INTO "roles" (name) VALUES ('ROLE_MODERATOR');
INSERT INTO "roles" (name) VALUES ('ROLE_USER');
INSERT INTO "roles" (name) VALUES ('ROLE_ADMIN');