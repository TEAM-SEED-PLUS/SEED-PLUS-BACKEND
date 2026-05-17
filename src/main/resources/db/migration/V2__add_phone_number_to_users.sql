ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(20) NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);

ALTER TABLE users
    DROP COLUMN email;

ALTER TABLE users
    ADD COLUMN birth_date DATE NOT NULL;
