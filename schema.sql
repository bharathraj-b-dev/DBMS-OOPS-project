CREATE DATABASE IF NOT EXISTS cinebook;
USE cinebook;

CREATE TABLE IF NOT EXISTS movie_show (
    show_id INT PRIMARY KEY,
    movie_name VARCHAR(100) NOT NULL,
    show_time VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS seat (
    show_id INT NOT NULL,
    seat_code VARCHAR(3) NOT NULL,
    seat_type VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'AVAILABLE',
    PRIMARY KEY (show_id, seat_code),
    FOREIGN KEY (show_id) REFERENCES movie_show(show_id)
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id INT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_code VARCHAR(3) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'CONFIRMED',
    FOREIGN KEY (show_id, seat_code) REFERENCES seat(show_id, seat_code),
    FOREIGN KEY (show_id) REFERENCES movie_show(show_id)
);

INSERT IGNORE INTO movie_show (show_id, movie_name, show_time) VALUES
(1, 'Interstellar', '10:00 AM'),
(2, 'Avengers', '2:00 PM'),
(3, 'Inception', '6:00 PM');

INSERT IGNORE INTO seat (show_id, seat_code, seat_type, status)
SELECT s.show_id,
       CONCAT(r.row_letter, n.seat_number),
       CASE
           WHEN r.row_letter IN ('A', 'B') THEN 'REGULAR'
           WHEN r.row_letter IN ('C', 'D') THEN 'PREMIUM'
           ELSE 'VIP'
       END,
       'AVAILABLE'
FROM movie_show s
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
