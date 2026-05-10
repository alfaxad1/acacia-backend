-- Migration script to transition from 'type' enum to 'fine_type_id' foreign key

-- 1. Create the fine_types table (if Hibernate hasn't auto-generated it yet)
CREATE TABLE IF NOT EXISTS fine_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(19, 2),
    percentage DECIMAL(19, 2)
);

-- 2. Insert the default Fine Types.
-- We try to fetch the default amounts from the sacco_setups table. If you have multiple setup rows, this uses the first one.
-- (Adjust the column names if they differ in your database)
INSERT INTO fine_types (name, description, amount, percentage)
SELECT 'LATE_PAYMENT', 'Late Payment Penalty', late_payment_fine_amount, 0 FROM sacco_setups LIMIT 1;

INSERT INTO fine_types (name, description, amount, percentage)
SELECT 'LATE_MEETINGS', 'Late for Meetings Fine', meeting_late_fine_amount, 0 FROM sacco_setups LIMIT 1;

INSERT INTO fine_types (name, description, amount, percentage)
SELECT 'MEETING_ABSENTEEISM', 'Meeting Absenteeism Fine', meeting_absent_fine_amount, 0 FROM sacco_setups LIMIT 1;

-- 3. Update the fines table to link existing records to the new fine_types
-- This assumes Hibernate has already added the 'fine_type_id' column to the 'fines' table.
-- If not, you may need to run: ALTER TABLE fines ADD COLUMN fine_type_id BIGINT;
UPDATE fines f
JOIN fine_types ft ON f.type = ft.name
SET f.fine_type_id = ft.id
WHERE f.type IS NOT NULL;

-- 4. After confirming data is successfully migrated, you can safely drop the old 'type' column
-- UNCOMMENT the following line when you are absolutely sure the migration succeeded:
-- ALTER TABLE fines DROP COLUMN type;
