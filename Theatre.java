import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Theatre {
    public List<Movie> getMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT movie_id, movie_name, genre, duration_minutes FROM movie ORDER BY movie_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("movie_id"),
                        rs.getString("movie_name"),
                        rs.getString("genre"),
                        rs.getInt("duration_minutes")
                ));
            }
        }
        return movies;
    }

    public List<Show> getShows() throws SQLException {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT s.show_id, s.movie_id, m.movie_name, s.show_date, s.show_time " +
                "FROM shows s JOIN movie m ON m.movie_id = s.movie_id ORDER BY s.show_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shows.add(new Show(
                        rs.getInt("show_id"),
                        rs.getInt("movie_id"),
                        rs.getString("movie_name"),
                        rs.getString("show_date"),
                        rs.getString("show_time")
                ));
            }
        }
        return shows;
    }

    public Show findShow(int showId) throws SQLException {
        String sql = "SELECT s.show_id, s.movie_id, m.movie_name, s.show_date, s.show_time " +
                "FROM shows s JOIN movie m ON m.movie_id = s.movie_id WHERE s.show_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Show(
                            rs.getInt("show_id"),
                            rs.getInt("movie_id"),
                            rs.getString("movie_name"),
                            rs.getString("show_date"),
                            rs.getString("show_time")
                    );
                }
            }
        }
        return null;
    }

    public void addMovie(String movieName, String genre, int durationMinutes) throws SQLException {
        String sql = "INSERT INTO movie (movie_name, genre, duration_minutes) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, movieName);
            ps.setString(2, genre);
            ps.setInt(3, durationMinutes);
            ps.executeUpdate();
        }
    }

    public void updateMovie(int movieId, String movieName, String genre, int durationMinutes) throws SQLException {
        String sql = "UPDATE movie SET movie_name = ?, genre = ?, duration_minutes = ? WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, movieName);
            ps.setString(2, genre);
            ps.setInt(3, durationMinutes);
            ps.setInt(4, movieId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Movie ID not found.");
            }
        }
    }

    public void deleteMovie(int movieId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM shows WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, movieId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Cannot delete movie because show timings already exist.");
                }
            }
        }

        String sql = "DELETE FROM movie WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Movie ID not found.");
            }
        }
    }

    public void addShow(int movieId, String showDate, String showTime) throws SQLException {
        String showSql = "INSERT INTO shows (movie_id, show_date, show_time) VALUES (?, ?, ?)";
        String seatSql = "INSERT INTO seat (show_id, seat_code, seat_type, status) VALUES (?, ?, ?, 'AVAILABLE')";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int showId;
                try (PreparedStatement ps = con.prepareStatement(showSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, movieId);
                    ps.setString(2, showDate);
                    ps.setString(3, showTime);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Show ID could not be generated.");
                        }
                        showId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(seatSql)) {
                    for (char row = 'A'; row <= 'E'; row++) {
                        for (int number = 1; number <= 8; number++) {
                            ps.setInt(1, showId);
                            ps.setString(2, row + Integer.toString(number));
                            ps.setString(3, SeatType.fromSeatCode(row + Integer.toString(number)).name());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void updateShow(int showId, int movieId, String showDate, String showTime) throws SQLException {
        String sql = "UPDATE shows SET movie_id = ?, show_date = ?, show_time = ? WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setString(2, showDate);
            ps.setString(3, showTime);
            ps.setInt(4, showId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Show ID not found.");
            }
        }
    }

    public void deleteShow(int showId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM booking WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, showId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Cannot delete this show because booking history exists.");
                }
            }
        }

        String sql = "DELETE FROM shows WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Show ID not found.");
            }
        }
    }

    public void displayMovies() throws SQLException {
        List<Movie> movies = getMovies();
        if (movies.isEmpty()) {
            System.out.println("No movies found.");
            return;
        }

        System.out.println("\n# ==================================================");
        System.out.println("MOVIE CATALOGUE");
        System.out.printf("%-10s %-30s %-20s %-12s%n", "Movie ID", "Movie Name", "Genre", "Duration");
        System.out.println("--------------------------------------------------");
        for (Movie movie : movies) {
            System.out.printf("%-10d %-30s %-20s %-12d%n",
                    movie.getMovieId(), movie.getMovieName(), movie.getGenre(), movie.getDurationMinutes());
        }
        System.out.println("==================================================");
    }

    public void displayShows() throws SQLException {
        List<Show> shows = getShows();
        if (shows.isEmpty()) {
            System.out.println("No show timings found.");
            return;
        }

        System.out.println("\n# ==================================================");
        System.out.println("SHOW TIMINGS");
        System.out.printf("%-8s %-28s %-12s %-12s%n", "Show ID", "Movie Name", "Date", "Time");
        System.out.println("--------------------------------------------------");
        for (Show show : shows) {
            System.out.printf("%-8d %-28s %-12s %-12s%n",
                    show.getShowId(), show.getMovieName(), show.getShowDate(), show.getShowTime());
        }
        System.out.println("==================================================");
    }

    public void displayBookingHistory() throws SQLException {
        String sql = "SELECT b.booking_id, m.movie_name, s.show_date, s.show_time, " +
                "GROUP_CONCAT(bs.seat_code ORDER BY bs.seat_code SEPARATOR ', ') AS selected_seats, " +
                "b.total_amount, b.status " +
                "FROM booking b " +
                "JOIN shows s ON s.show_id = b.show_id " +
                "JOIN movie m ON m.movie_id = s.movie_id " +
                "LEFT JOIN booking_seat bs ON bs.booking_id = b.booking_id " +
                "GROUP BY b.booking_id, m.movie_name, s.show_date, s.show_time, b.total_amount, b.status " +
                "ORDER BY b.booking_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n# ==================================================");
            System.out.println("BOOKING HISTORY");
            System.out.printf("%-10s %-25s %-12s %-12s %-22s %-12s %-12s%n",
                    "Booking ID", "Movie", "Date", "Time", "Seats", "Amount", "Status");
            System.out.println("--------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d %-25s %-12s %-12s %-22s Rs. %-9.2f %-12s%n",
                        rs.getInt("booking_id"),
                        rs.getString("movie_name"),
                        rs.getString("show_date"),
                        rs.getString("show_time"),
                        rs.getString("selected_seats"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"));
            }
            if (!found) {
                System.out.println("No booking records found.");
            }
            System.out.println("==================================================");
        }
    }

    public void displayRevenueReport() throws SQLException {
        String summarySql = "SELECT COUNT(*) AS confirmed_bookings, " +
                "COALESCE(SUM(ticket_count), 0) AS total_tickets, " +
                "COALESCE(SUM(total_amount), 0) AS total_revenue " +
                "FROM ( " +
                "SELECT b.booking_id, b.total_amount, COUNT(bs.seat_code) AS ticket_count " +
                "FROM booking b LEFT JOIN booking_seat bs ON bs.booking_id = b.booking_id " +
                "WHERE b.status = 'CONFIRMED' GROUP BY b.booking_id, b.total_amount " +
                ") t";

        String movieSql = "SELECT m.movie_name, COUNT(DISTINCT b.booking_id) AS confirmed_bookings, " +
                "SUM(seat_counts.seat_count) AS total_tickets, " +
                "SUM(b.total_amount) AS revenue " +
                "FROM booking b " +
                "JOIN shows s ON s.show_id = b.show_id " +
                "JOIN movie m ON m.movie_id = s.movie_id " +
                "JOIN (SELECT booking_id, COUNT(seat_code) AS seat_count FROM booking_seat GROUP BY booking_id) AS seat_counts " +
                "ON seat_counts.booking_id = b.booking_id " +
                "WHERE b.status = 'CONFIRMED' " +
                "GROUP BY m.movie_id, m.movie_name " +
                "ORDER BY m.movie_name";

        System.out.println("\n# ==================================================");
        System.out.println("REVENUE REPORT");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(summarySql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.printf("Confirmed bookings : %d%n", rs.getInt("confirmed_bookings"));
                System.out.printf("Tickets sold       : %d%n", rs.getInt("total_tickets"));
                System.out.printf("Total revenue      : Rs. %.2f%n", rs.getDouble("total_revenue"));
            }
        }

        System.out.println("\nRevenue by movie:");
        System.out.printf("%-28s %-18s %-14s %-12s%n", "Movie", "Bookings", "Tickets", "Revenue");
        System.out.println("--------------------------------------------------");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(movieSql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-28s %-18d %-14d Rs. %-10.2f%n",
                        rs.getString("movie_name"),
                        rs.getInt("confirmed_bookings"),
                        rs.getInt("total_tickets"),
                        rs.getDouble("revenue"));
            }
            if (!found) {
                System.out.println("No confirmed bookings yet.");
            }
        }
        System.out.println("==================================================");
    }

    public void displaySeatOccupancy() throws SQLException {
        String sql = "SELECT s.show_id, m.movie_name, COUNT(seat.seat_code) AS total_seats, " +
                "SUM(CASE WHEN seat.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_seats, " +
                "(COUNT(seat.seat_code) - SUM(CASE WHEN seat.status = 'BOOKED' THEN 1 ELSE 0 END)) AS available_seats, " +
                "ROUND((SUM(CASE WHEN seat.status = 'BOOKED' THEN 1 ELSE 0 END) * 100.0) / COUNT(seat.seat_code), 2) AS occupancy_pct " +
                "FROM shows s " +
                "JOIN movie m ON m.movie_id = s.movie_id " +
                "LEFT JOIN seat ON seat.show_id = s.show_id " +
                "GROUP BY s.show_id, m.movie_name " +
                "ORDER BY s.show_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n# ==================================================");
            System.out.println("SEAT OCCUPANCY REPORT");
            System.out.printf("%-8s %-28s %-12s %-12s %-14s %-12s%n",
                    "Show ID", "Movie", "Total", "Booked", "Available", "Occupancy%");
            System.out.println("--------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d %-28s %-12d %-12d %-14d %-12.2f%n",
                        rs.getInt("show_id"),
                        rs.getString("movie_name"),
                        rs.getInt("total_seats"),
                        rs.getInt("booked_seats"),
                        rs.getInt("available_seats"),
                        rs.getDouble("occupancy_pct"));
            }
            if (!found) {
                System.out.println("No show occupancy data available.");
            }
            System.out.println("==================================================");
        }
    }

    public void printBookingTable(String sql) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("Booking: %d | Movie: %s | Seats: %s | Amount: Rs. %.2f | Status: %s%n",
                        rs.getInt("booking_id"),
                        rs.getString("movie_name"),
                        rs.getString("selected_seats"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"));
            }
        }
    }
}
