CREATE DATABASE IF NOT EXISTS cinebook;
USE cinebook;

CREATE TABLE IF NOT EXISTS movie (
    movie_id INT NOT NULL AUTO_INCREMENT,
    movie_name VARCHAR(150) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    duration_minutes INT NOT NULL,
    PRIMARY KEY (movie_id),
    UNIQUE (movie_name)
);

CREATE TABLE IF NOT EXISTS shows (
    show_id INT NOT NULL AUTO_INCREMENT,
    movie_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    PRIMARY KEY (show_id),
    UNIQUE (movie_id, show_date, show_time),
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movie (movie_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS seat (
    show_id INT NOT NULL,
    seat_code VARCHAR(3) NOT NULL,
    seat_type VARCHAR(10) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'AVAILABLE',
    PRIMARY KEY (show_id, seat_code),
    CONSTRAINT chk_seat_code CHECK (seat_code REGEXP '^[A-E][1-8]$'),
    CONSTRAINT chk_seat_status CHECK (status IN ('AVAILABLE', 'BOOKED')),
    CONSTRAINT fk_seat_show FOREIGN KEY (show_id) REFERENCES shows (show_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS booking (
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

CREATE TABLE IF NOT EXISTS booking_seat (
    booking_id INT NOT NULL,
    show_id INT NOT NULL,
    seat_code VARCHAR(3) NOT NULL,
    seat_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (booking_id, seat_code),
    CONSTRAINT fk_booking_seat_booking FOREIGN KEY (booking_id) REFERENCES booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_seat_seat FOREIGN KEY (show_id, seat_code) REFERENCES seat (show_id, seat_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

INSERT IGNORE INTO movie (movie_id, movie_name, genre, duration_minutes) VALUES
(1, 'Interstellar', 'Sci-Fi', 169),
(2, 'Avengers: Endgame', 'Action', 181),
(3, 'Inception', 'Sci-Fi', 148);

INSERT IGNORE INTO shows (show_id, movie_id, show_date, show_time) VALUES
(1, 1, CURDATE(), '10:00:00'),
(2, 2, CURDATE(), '14:00:00'),
(3, 3, CURDATE(), '18:00:00');

INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status)
SELECT s.show_id,
       CONCAT(r.row_letter, n.seat_number),
       CASE
           WHEN r.row_letter IN ('A', 'B') THEN 'REGULAR'
           WHEN r.row_letter IN ('C', 'D') THEN 'PREMIUM'
           ELSE 'VIP'
       END,
       'AVAILABLE'
FROM shows s
CROSS JOIN (
    SELECT 'A' AS row_letter UNION ALL
    SELECT 'B' UNION ALL
    SELECT 'C' UNION ALL
    SELECT 'D' UNION ALL
    SELECT 'E'
) r
CROSS JOIN (
    SELECT 1 AS seat_number UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) n;
