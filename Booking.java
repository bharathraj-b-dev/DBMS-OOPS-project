import java.util.List;

public class Booking {
    private final int bookingId;
    private final int showId;
    private final String movieName;
    private final String showDate;
    private final String showTime;
    private final List<String> seatCodes;
    private final double totalAmount;
    private final String status;

    public Booking(int bookingId, int showId, String movieName, String showDate,
                   String showTime, List<String> seatCodes, double totalAmount, String status) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.movieName = movieName;
        this.showDate = showDate;
        this.showTime = showTime;
        this.seatCodes = seatCodes;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getShowId() {
        return showId;
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

    public List<String> getSeatCodes() {
        return seatCodes;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
