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