import java.sql.SQLException;
import java.util.List;

public interface Bookable {
    Booking bookSeats(List<String> seatCodes, String couponCode) throws SQLException;
    double cancelBooking(int bookingId) throws SQLException;
}
