import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CineApp {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Theatre theatre = new Theatre();

        while (true) {
            System.out.println("\n==============================");
            System.out.println("          CINEBOOK");
            System.out.println("==============================");
            System.out.println("1. View shows and book tickets");
            System.out.println("2. Cancel a booking");
            System.out.println("3. View booking records");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        bookTickets(theatre);
                        break;
                    case "2":
                        cancelBooking(theatre);
                        break;
                    case "3":
                        theatre.displayBookings();
                        break;
                    case "4":
                        System.out.println("Thank you for using CineBook!");
                        return;
                    default:
                        System.out.println("Invalid choice. Enter 1, 2, 3, or 4.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Check that MySQL is running and DBConnection.java has the correct password.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void bookTickets(Theatre theatre) throws SQLException {
        List<Show> shows = theatre.getShows();
        System.out.println("\nAvailable shows:");
        for (Show show : shows) {
            System.out.printf("%d. %s - %s%n",
                    show.getShowId(), show.getMovieName(), show.getShowTime());
        }

        System.out.print("Enter show ID: ");
        int showId = Integer.parseInt(scanner.nextLine().trim());
        Show show = theatre.findShow(showId);

        if (show == null) {
            System.out.println("Invalid show ID.");
            return;
        }

        show.displaySeatMap();
        System.out.print("Enter seats separated by commas (e.g., C3,C4): ");
        String input = scanner.nextLine().trim().toUpperCase();
        List<String> seatCodes = new ArrayList<>();
        for (String code : input.split(",")) {
            String seat = code.trim();
            if (!seat.matches("[A-E][1-8]")) {
                throw new IllegalArgumentException("Invalid seat code: " + seat);
            }
            if (seatCodes.contains(seat)) {
                throw new IllegalArgumentException("Duplicate seat code: " + seat);
            }
            seatCodes.add(seat);
        }

        System.out.print("Coupon code (press Enter to skip): ");
        String coupon = scanner.nextLine().trim();

        Booking booking = show.bookSeats(seatCodes, coupon);
        System.out.println("\n========== BOOKING SUMMARY ==========");
        System.out.println("Booking ID : BK" + booking.getBookingId());
        System.out.println("Movie      : " + booking.getMovieName());
        System.out.println("Show time  : " + booking.getShowTime());
        System.out.println("Seats      : " + String.join(", ", booking.getSeatCodes()));
        System.out.printf("Total      : Rs. %.2f%n", booking.getAmount());
        if ("CINE10".equalsIgnoreCase(coupon)) {
            System.out.println("Coupon     : CINE10 (10% discount)");
        }
        System.out.println("Booking successful!");
    }

    private static void cancelBooking(Theatre theatre) throws SQLException {
        System.out.print("Enter show ID for the booking: ");
        int showId = Integer.parseInt(scanner.nextLine().trim());
        Show show = theatre.findShow(showId);
        if (show == null) {
            System.out.println("Invalid show ID.");
            return;
        }

        System.out.print("Enter booking ID (numbers only, e.g., 1001): ");
        int bookingId = Integer.parseInt(scanner.nextLine().trim());
        double refund = show.cancelBooking(bookingId);
        System.out.printf("Booking cancelled. Refund amount: Rs. %.2f%n", refund);
    }
}
