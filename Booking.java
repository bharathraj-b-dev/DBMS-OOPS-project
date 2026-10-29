import java.util.List;

public class Booking {
    private static int nextBookingId = 1001;

    private final int bookingId;
    private final int showId;
    private final String movieName;
    private final String showTime;
    private final List<String> seatCodes;
    private final double amount;

    public Booking(int showId, String movieName, String showTime,
                   List<String> seatCodes, double amount) {
        this.bookingId = nextBookingId++;
        this.showId = showId;
        this.movieName = movieName;
        this.showTime = showTime;
        this.seatCodes = seatCodes;
        this.amount = amount;
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

    public String getShowTime() {
        return showTime;
    }

    public List<String> getSeatCodes() {
        return seatCodes;
    }

    public double getAmount() {
        return amount;
    }
}
