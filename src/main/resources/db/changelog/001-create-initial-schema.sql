-- liquibase formatted sql
-- changeset author:admin:001-create-initial-schema

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE units
(
    id                 BIGSERIAL PRIMARY KEY,
    number_of_rooms    INT            NOT NULL,
    accommodation_type VARCHAR(10)    NOT NULL,
    floor              INT            NOT NULL,
    base_cost          DECIMAL(12, 2) NOT NULL,
    description        TEXT,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings
(
    id             BIGSERIAL PRIMARY KEY,
    unit_id        BIGINT         NOT NULL,
    user_id        BIGINT         NOT NULL,
    check_in_date  DATE           NOT NULL,
    check_out_date DATE           NOT NULL,
    status         VARCHAR(10)    NOT NULL,
    total_cost     DECIMAL(12, 2) NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE,
    expires_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_booking_unit FOREIGN KEY (unit_id) REFERENCES units (id),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

CREATE TABLE payments
(
    id         BIGSERIAL PRIMARY KEY,
    booking_id BIGINT         NOT NULL,
    status     VARCHAR(10)    NOT NULL,
    amount     DECIMAL(12, 2) NOT NULL,
    paid_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE TABLE unit_events
(
    id         BIGSERIAL PRIMARY KEY,
    unit_id    BIGINT      NOT NULL,
    booking_id BIGINT,
    event_type VARCHAR(20) NOT NULL,
    details    TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_unit FOREIGN KEY (unit_id) REFERENCES units (id) ON DELETE SET NULL,
    CONSTRAINT fk_event_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL
);

CREATE INDEX idx_booking_unit_id ON bookings (unit_id);
CREATE INDEX idx_booking_user_id ON bookings (user_id);
CREATE INDEX idx_booking_dates ON bookings (check_in_date, check_out_date);
CREATE INDEX idx_booking_status_expires ON bookings (status, expires_at);
CREATE INDEX idx_unit_properties ON units (number_of_rooms, accommodation_type, floor, base_cost);
