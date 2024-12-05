
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
    user_id integer not null,
    CONSTRAINT fk_jwt_to_user_entity  foreign key (user_id) references "user_entity"(id)
);

CREATE TABLE IF NOT EXISTS "validations"(
    id serial primary key,
    creation TIMESTAMPTZ  not null,
    expired TIMESTAMPTZ  not null,
    activation TIMESTAMPTZ ,
    code varchar(100) not null,
    uid VARCHAR(50) not null,
    user_id integer not null,
    CONSTRAINT fk_validation_to_user_entity foreign key (user_id) references "user_entity"(id)
);
CREATE Table IF NOT EXISTS "user_roles"(
    id serial primary key,
    user_id integer not null,
    roles_id integer not null,
    CONSTRAINT fk_user_roles_to_user_entity foreign key (user_id) references "user_entity"(id),
    CONSTRAINT fk_user_roles_to_roles foreign key (roles_id) references "roles"(id)
);
CREATE TABLE IF NOT EXISTS "file_info"(
    id serial primary key,
    file_name varchar(100) not null,
    file_type varchar(40) not null,
    creation_date TIMESTAMPTZ not null,
    expiration_date TIMESTAMPTZ not null
);
CREATE TABLE IF NOT EXISTS "invoice"(
    id serial primary key,
    invoice_number varchar(50),
    tmp_invoice_number varchar(50) not null,
    seller_name varchar(100) not null,
    seller_email varchar(200) not null,
    seller_address varchar(200) not null,
    seller_phone varchar(20) not null,
    seller_siret varchar(30) not null,
    customer_name varchar(40) not null,
    customer_email varchar(200) not null,
    customer_address varchar(200) not null,
    customer_phone varchar(20) not null,
    creation_date TIMESTAMPTZ not null,
    expiration_date TIMESTAMPTZ not null,
    status varchar(50) not null,
    amount_ht NUMERIC(15,2) not null,
    amount_ttc NUMERIC(15,2) not null,
    user_id integer not null,
    file_id integer not null,
    CONSTRAINT fk_invoice_to_user FOREIGN KEY (user_id) REFERENCES user_entity(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_to_file FOREIGN KEY (file_id) REFERENCES file_info(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS "items" (
    id SERIAL PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    price_ht NUMERIC(15,2) NOT NULL,
    price_ttc NUMERIC(15,2) NOT NULL,
    tax NUMERIC(5,2) NOT NULL,
    quantity INT NOT NULL,
    total_ht NUMERIC(15,2) NOT NULL,
    total_ttc NUMERIC(15,2) NOT NULL,
    description TEXT,
    invoice_id INT NOT NULL,
    CONSTRAINT fk_items_to_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE
);

INSERT INTO "roles" (name) VALUES ('ROLE_MODERATOR')
ON CONFLICT (name) DO NOTHING;
INSERT INTO "roles" (name) VALUES ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;
INSERT INTO "roles" (name) VALUES ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;