import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Show implements Bookable {
    private final int showId;
    private final String movieName;
    private final String showTime;

    public Show(int showId, String movieName, String showTime) {
        this.showId = showId;
        this.movieName = movieName;
        this.showTime = showTime;
    }

    public int getShowId() {
        return showId;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getShowTime() {
        return showTime;
    }

    public void displaySeatMap() throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("\n       " + movieName + " - " + showTime);
            System.out.println("    1 2 3 4 5 6 7 8");

            String sql = "SELECT seat_code, status FROM seat WHERE show_id = ? ORDER BY seat_code";
            String[][] grid = new String[5][8];

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, showId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("seat_code");
                        int row = code.charAt(0) - 'A';
                        int col = Integer.parseInt(code.substring(1)) - 1;
                        grid[row][col] = rs.getString("status").equals("AVAILABLE") ? "O" : "X";
                    }
                }
            }

            for (int row = 0; row < 5; row++) {
                System.out.print((char) ('A' + row) + "   ");
                for (int col = 0; col < 8; col++) {
                    System.out.print(grid[row][col] + " ");
                }
                System.out.println();
            }
            System.out.println("O = Available, X = Booked");
        }
    }

    @Override
    public Booking bookSeats(List<String> seatCodes, String couponCode) throws SQLException {
        if (seatCodes.isEmpty()) {
            throw new IllegalArgumentException("Please enter at least one seat.");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                double total = 0;
                List<Double> seatPrices = new ArrayList<>();

                // Validate every seat first. No seat is changed until all are valid.
                String checkSql = "SELECT seat_type, status FROM seat WHERE show_id = ? AND seat_code = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    for (String code : seatCodes) {
                        ps.setInt(1, showId);
                        ps.setString(2, code);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new IllegalArgumentException("Invalid seat: " + code);
                            }
                            if (!rs.getString("status").equals("AVAILABLE")) {
                                throw new IllegalArgumentException("Seat already booked: " + code);
                            }
                            double price = SeatType.fromString(rs.getString("seat_type")).getPrice();
                            seatPrices.add(price);
                            total += price;
                        }
                    }
                }

                Booking booking = new Booking(showId, movieName, showTime, new ArrayList<>(seatCodes),
                        calculateTotal(total, couponCode));

                String updateSql = "UPDATE seat SET status = 'BOOKED' WHERE show_id = ? AND seat_code = ?";
                String insertSql = "INSERT INTO booking (booking_id, show_id, seat_code, amount, status) VALUES (?, ?, ?, ?, 'CONFIRMED')";

                try (PreparedStatement update = con.prepareStatement(updateSql);
                     PreparedStatement insert = con.prepareStatement(insertSql)) {
                    for (int i = 0; i < seatCodes.size(); i++) {
                        update.setInt(1, showId);
                        update.setString(2, seatCodes.get(i));
                        update.executeUpdate();

                        insert.setInt(1, booking.getBookingId());
                        insert.setInt(2, showId);
                        insert.setString(3, seatCodes.get(i));
                        insert.setDouble(4, booking.getAmount() / seatCodes.size());
                        insert.executeUpdate();
                    }
                }

                con.commit();
                return booking;
            } catch (Exception e) {
                con.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                throw new SQLException("Booking failed.", e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public double calculateTotal(double total) {
        return total;
    }

    public double calculateTotal(double total, String couponCode) {
        if (couponCode != null && couponCode.trim().equalsIgnoreCase("CINE10")) {
            return total * 0.90;
        }
        return total;
    }

    @Override
    public double cancelBooking(int bookingId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String findSql = "SELECT show_id, seat_code, amount, status FROM booking WHERE booking_id = ?";
                List<String> seatCodes = new ArrayList<>();
                double totalAmount = 0;
                boolean found = false;

                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            found = true;
                            if (!rs.getString("status").equals("CONFIRMED")) {
                                throw new IllegalArgumentException("This booking is already cancelled.");
                            }
                            if (rs.getInt("show_id") != showId) {
                                throw new IllegalArgumentException("Booking does not belong to this show.");
                            }
                            seatCodes.add(rs.getString("seat_code"));
                            totalAmount += rs.getDouble("amount");
                        }
                    }
                }

                if (!found) {
                    throw new IllegalArgumentException("Booking ID not found.");
                }

                try (PreparedStatement updateSeat = con.prepareStatement(
                        "UPDATE seat SET status = 'AVAILABLE' WHERE show_id = ? AND seat_code = ?");
                     PreparedStatement updateBooking = con.prepareStatement(
                        "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = ?")) {
                    for (String code : seatCodes) {
                        updateSeat.setInt(1, showId);
                        updateSeat.setString(2, code);
                        updateSeat.executeUpdate();
                    }
                    updateBooking.setInt(1, bookingId);
                    updateBooking.executeUpdate();
                }

                con.commit();
                return totalAmount * 0.80;
            } catch (Exception e) {
                con.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                throw new SQLException("Cancellation failed.", e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
