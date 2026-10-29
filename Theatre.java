import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Theatre {
    public List<Show> getShows() throws SQLException {
        List<Show> shows = new ArrayList<>();
        String sql = "SELECT show_id, movie_name, show_time FROM movie_show ORDER BY show_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shows.add(new Show(rs.getInt("show_id"),
                        rs.getString("movie_name"),
                        rs.getString("show_time")));
            }
        }
        return shows;
    }

    public Show findShow(int showId) throws SQLException {
        String sql = "SELECT show_id, movie_name, show_time FROM movie_show WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Show(rs.getInt("show_id"),
                            rs.getString("movie_name"),
                            rs.getString("show_time"));
                }
            }
        }
        return null;
    }

    public void displayBookings() throws SQLException {
        String sql = "SELECT booking_id, show_id, seat_code, amount, status FROM booking ORDER BY booking_id, seat_code";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nBooking records:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("ID: BK%d | Show: %d | Seat: %s | Amount: Rs. %.2f | %s%n",
                        rs.getInt("booking_id"), rs.getInt("show_id"), rs.getString("seat_code"),
                        rs.getDouble("amount"), rs.getString("status"));
            }
            if (!any) {
                System.out.println("No bookings found.");
            }
        }
    }
}
