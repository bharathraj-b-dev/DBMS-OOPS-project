import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Show implements Bookable {
    private final int showId;
    private final int movieId;
    private final String movieName;
    private final String showDate;
    private final String showTime;

    public Show(int showId, int movieId, String movieName, String showDate, String showTime) {
        this.showId = showId;
        this.movieId = movieId;
        this.movieName = movieName;
        this.showDate = showDate;
        this.showTime = showTime;
    }

    public int getShowId() {
        return showId;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getShowDate() {
        return showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void displaySeatMap() throws SQLException {
        String sql = "SELECT seat_code, status, seat_type FROM seat WHERE show_id = ? ORDER BY seat_code";
        String[][] grid = new String[5][8];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = "O";
            }
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("seat_code");
                    int row = code.charAt(0) - 'A';
                    int col = Integer.parseInt(code.substring(1)) - 1;
                    grid[row][col] = "AVAILABLE".equalsIgnoreCase(rs.getString("status")) ? "O" : "X";
                }
            }
        }

        System.out.println("\n# ==================================================");
        System.out.println("SEAT AVAILABILITY");
        System.out.printf("%s - %s %s%n", movieName, showDate, showTime);
        System.out.println("          1   2   3   4   5   6   7   8");

        for (int row = 0; row < 5; row++) {
            System.out.print("      " + (char) ('A' + row) + "   ");
            for (int col = 0; col < 8; col++) {
                System.out.print(grid[row][col] + "   ");
            }
            System.out.println();
        }

        System.out.println();
        System.out.println("O = Available   X = Booked");
        System.out.println("A-B : Regular  - Rs. 150");
        System.out.println("C-D : Premium  - Rs. 250");
        System.out.println("E   : VIP      - Rs. 400");
        System.out.println("==================================================");
    }

    @Override
    public Booking bookSeats(List<String> seatCodes, String couponCode) throws SQLException {
        if (seatCodes == null || seatCodes.isEmpty()) {
            throw new IllegalArgumentException("Please choose at least one seat.");
        }

        List<String> normalized = new ArrayList<>();
        for (String seatCode : seatCodes) {
            String code = seatCode.trim().toUpperCase();
            if (!code.matches("^[A-E][1-8]$")) {
                throw new IllegalArgumentException("Invalid seat code: " + seatCode);
            }
            if (normalized.contains(code)) {
                throw new IllegalArgumentException("Duplicate seat selection: " + code);
            }
            normalized.add(code);
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String checkSql = "SELECT seat_code, seat_type, status FROM seat WHERE show_id = ? AND seat_code = ? FOR UPDATE";
                double subtotal = 0;
                List<String> selectedSeats = new ArrayList<>();
                List<String> seatTypes = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    for (String code : normalized) {
                        ps.setInt(1, showId);
                        ps.setString(2, code);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new IllegalArgumentException("Seat " + code + " is not valid for this show.");
                            }
                            String seatType = rs.getString("seat_type");
                            String status = rs.getString("status");
                            if (!"AVAILABLE".equalsIgnoreCase(status)) {
                                throw new IllegalArgumentException("Seat " + code + " is already booked.");
                            }
                            selectedSeats.add(code);
                            seatTypes.add(seatType);
                            subtotal += SeatType.fromString(seatType).getPrice();
                        }
                    }
                }

                double totalAmount = calculateTotal(subtotal, couponCode);
                String bookingSql = "INSERT INTO booking (show_id, total_amount, status, coupon_code) VALUES (?, ?, 'CONFIRMED', ?)";
                int bookingId;

                try (PreparedStatement insertBooking = con.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertBooking.setInt(1, showId);
                    insertBooking.setBigDecimal(2, BigDecimal.valueOf(totalAmount));
                    insertBooking.setString(3, (couponCode == null || couponCode.isBlank()) ? null : couponCode.trim());
                    insertBooking.executeUpdate();

                    try (ResultSet generatedKeys = insertBooking.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Booking ID could not be generated.");
                        }
                        bookingId = generatedKeys.getInt(1);
                    }
                }

                String insertSeatSql = "INSERT INTO booking_seat (booking_id, show_id, seat_code, seat_price) VALUES (?, ?, ?, ?)";
                String updateSeatSql = "UPDATE seat SET status = 'BOOKED' WHERE show_id = ? AND seat_code = ?";

                try (PreparedStatement insertSeat = con.prepareStatement(insertSeatSql);
                     PreparedStatement updateSeat = con.prepareStatement(updateSeatSql)) {
                    for (int i = 0; i < selectedSeats.size(); i++) {
                        String code = selectedSeats.get(i);
                        double price = SeatType.fromString(seatTypes.get(i)).getPrice();

                        insertSeat.setInt(1, bookingId);
                        insertSeat.setInt(2, showId);
                        insertSeat.setString(3, code);
                        insertSeat.setBigDecimal(4, BigDecimal.valueOf(price));
                        insertSeat.executeUpdate();

                        updateSeat.setInt(1, showId);
                        updateSeat.setString(2, code);
                        updateSeat.executeUpdate();
                    }
                }

                con.commit();
                return new Booking(bookingId, showId, movieName, showDate, showTime, new ArrayList<>(selectedSeats), totalAmount, "CONFIRMED");
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } catch (IllegalArgumentException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public double calculateTotal(double total) {
        return total;
    }

    public double calculateTotal(double total, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return total;
        }
        if (couponCode.trim().equalsIgnoreCase("CINE10")) {
            return total * 0.90;
        }
        throw new IllegalArgumentException("Invalid coupon code. Use CINE10 or leave it blank.");
    }

    @Override
    public double cancelBooking(int bookingId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String findSql = "SELECT b.booking_id, b.total_amount, b.status, bs.seat_code " +
                        "FROM booking b LEFT JOIN booking_seat bs ON bs.booking_id = b.booking_id " +
                        "WHERE b.booking_id = ? AND b.show_id = ? FOR UPDATE";

                List<String> seatCodes = new ArrayList<>();
                double amount = 0;
                boolean bookingExists = false;

                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, showId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            bookingExists = true;
                            String status = rs.getString("status");
                            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                                throw new IllegalArgumentException("This booking is already cancelled.");
                            }
                            if (rs.getString("seat_code") != null) {
                                seatCodes.add(rs.getString("seat_code"));
                            }
                            amount = rs.getDouble("total_amount");
                        }
                    }
                }

                if (!bookingExists) {
                    throw new IllegalArgumentException("Booking ID not found for this show.");
                }

                String updateSeatSql = "UPDATE seat SET status = 'AVAILABLE' WHERE show_id = ? AND seat_code = ?";
                String updateBookingSql = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = ?";

                try (PreparedStatement updateSeat = con.prepareStatement(updateSeatSql);
                     PreparedStatement updateBooking = con.prepareStatement(updateBookingSql)) {
                    for (String seatCode : seatCodes) {
                        updateSeat.setInt(1, showId);
                        updateSeat.setString(2, seatCode);
                        updateSeat.executeUpdate();
                    }
                    updateBooking.setInt(1, bookingId);
                    updateBooking.executeUpdate();
                }

                con.commit();
                return amount * 0.80;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } catch (IllegalArgumentException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
