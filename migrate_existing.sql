USE cinebook;

-- Run this once after the updated schema.sql was run over the original project database.
-- The original tables are renamed and retained as backups; no booking rows are deleted.
RENAME TABLE booking_seat TO booking_seat_before_migration,
             booking TO booking_before_migration;

ALTER TABLE seat DROP FOREIGN KEY seat_ibfk_1;
ALTER TABLE seat
    ADD CONSTRAINT fk_seat_show
    FOREIGN KEY (show_id) REFERENCES shows (show_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

CREATE TABLE booking (
    booking_id INT NOT NULL AUTO_INCREMENT,
    show_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'CONFIRMED',
    coupon_code VARCHAR(20) NULL,
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (booking_id),
    CONSTRAINT chk_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows (show_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE booking_seat (
    booking_id INT NOT NULL,
    show_id INT NOT NULL,
    seat_code VARCHAR(3) NOT NULL,
    seat_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (booking_id, seat_code),
    CONSTRAINT fk_booking_seat_booking_migrated FOREIGN KEY (booking_id) REFERENCES booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_seat_seat_migrated FOREIGN KEY (show_id, seat_code) REFERENCES seat (show_id, seat_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

INSERT INTO booking (booking_id, show_id, total_amount, status)
SELECT booking_id,
       show_id,
       SUM(amount),
       CASE WHEN UPPER(status) = 'CANCELLED' THEN 'CANCELLED' ELSE 'CONFIRMED' END
FROM booking_before_migration
GROUP BY booking_id, show_id, status;

INSERT INTO booking_seat (booking_id, show_id, seat_code, seat_price)
SELECT booking_id, show_id, seat_code, amount
FROM booking_before_migration;
