
CREATE TABLE IF NOT EXISTS "roles"(
    id serial primary key,
    name varchar(20) UNIQUE not null
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
    creation TIMESTAMPTZ  not null,
    expired TIMESTAMPTZ  not null,
    activation TIMESTAMPTZ ,
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
CREATE TABLE IF NOT EXISTS "invoice_info"(
    id serial primary key,
    invoice_number varchar(50) not null,
    invoice_customer varchar(40) not null,
    invoice_date varchar(50) not null,
    invoice_amount varchar(30) not null,
    invoice_expir_date varchar(50) not null,
    status varchar(50) not null,
    user_id integer not null,
    FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE
);
INSERT INTO "roles" (name) VALUES ('ROLE_MODERATOR')
ON CONFLICT (name) DO NOTHING;
INSERT INTO "roles" (name) VALUES ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;
INSERT INTO "roles" (name) VALUES ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;
