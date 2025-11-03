-- liquibase formatted sql
-- changeset author:admin:002-insert-initial-data

-- Create a default user
INSERT INTO users (username, email, created_at)
VALUES ('John Doe', 'john@example.com', CURRENT_TIMESTAMP);

-- Insert 10 sample units
INSERT INTO units (number_of_rooms, accommodation_type, floor, base_cost, description, created_at)
VALUES (2, 'FLAT', 3, 120.00, 'Cozy flat in the city center', CURRENT_TIMESTAMP),
       (1, 'APARTMENTS', 10, 85.50, 'Studio apartment with a great view', CURRENT_TIMESTAMP),
       (5, 'HOME', 1, 350.00, 'Spacious family home with a garden', CURRENT_TIMESTAMP),
       (3, 'FLAT', 5, 150.00, 'Modern flat, fully equipped', CURRENT_TIMESTAMP),
       (1, 'APARTMENTS', 2, 75.00, 'Small studio, perfect for solo travelers', CURRENT_TIMESTAMP),
       (2, 'APARTMENTS', 7, 110.00, 'Luxury apartments with pool access', CURRENT_TIMESTAMP),
       (4, 'HOME', 2, 280.00, 'Two-story home in a quiet neighborhood', CURRENT_TIMESTAMP),
       (2, 'FLAT', 1, 95.00, 'Ground floor flat with easy access', CURRENT_TIMESTAMP),
       (1, 'FLAT', 4, 90.00, 'Bright one-bedroom flat', CURRENT_TIMESTAMP),
       (3, 'APARTMENTS', 12, 200.00, 'Penthouse apartment with stunning views', CURRENT_TIMESTAMP);

-- Insert creation events for the 10 units
-- This assumes the IDs are 1 through 10
INSERT INTO unit_events (unit_id, event_type, details, created_at)
VALUES (1, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (2, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (3, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (4, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (5, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (6, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (7, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (8, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (9, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP),
       (10, 'UNIT_CREATED', 'Unit created via Liquibase migration', CURRENT_TIMESTAMP);
