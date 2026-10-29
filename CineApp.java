import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        Theatre theatre = new Theatre();
        while (true) {
            printMainMenu();
            String choice = readNonEmptyInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1":
                        customerMenu(theatre);
                        break;
                    case "2":
                        adminMenu(theatre);
                        break;
                    case "3":
                        System.out.println("\nThank you for using CineBook. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please choose 1, 2 or 3.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void customerMenu(Theatre theatre) throws SQLException {
        while (true) {
            printCustomerMenu();
            String choice = readNonEmptyInput("Enter your choice: ");

            switch (choice) {
                case "1":
                    theatre.displayMovies();
                    break;
                case "2":
                    theatre.displayShows();
                    break;
                case "3":
                    viewSeatAvailability(theatre);
                    break;
                case "4":
                    bookTickets(theatre);
                    break;
                case "5":
                    cancelBooking(theatre);
                    break;
                case "6":
                    theatre.displayBookingHistory();
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid selection. Please choose 1 to 7.");
            }
        }
    }

    private static void adminMenu(Theatre theatre) throws SQLException {
        System.out.print("Enter administrator password: ");
        String password = scanner.nextLine().trim();
        if (!ADMIN_PASSWORD.equals(password)) {
            System.out.println("Invalid administrator password.");
            return;
        }

        while (true) {
            printAdminMenu();
            String choice = readNonEmptyInput("Enter your choice: ");

            try {
                switch (choice) {
                    case "1":
                        addMovie(theatre);
                        break;
                    case "2":
                        theatre.displayMovies();
                        break;
                    case "3":
                        updateMovie(theatre);
                        break;
                    case "4":
                        removeMovie(theatre);
                        break;
                    case "5":
                        manageShowTimings(theatre);
                        break;
                    case "6":
                        theatre.displayBookingHistory();
                        break;
                    case "7":
                        theatre.displayRevenueReport();
                        break;
                    case "8":
                        theatre.displaySeatOccupancy();
                        break;
                    case "9":
                        return;
                    default:
                        System.out.println("Invalid selection. Please choose 1 to 9.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n# ==================================================");
        System.out.println("CINEBOOK");
        System.out.println("MOVIE BOOKING SYSTEM");
        System.out.println("1. Customer");
        System.out.println("2. Administrator");
        System.out.println("3. Exit");
        System.out.println("==================================================");
    }

    private static void printCustomerMenu() {
        System.out.println("\n# ==================================================");
        System.out.println("CUSTOMER MENU");
        System.out.println("1. View Movies");
        System.out.println("2. View Show Timings");
        System.out.println("3. View Seat Availability");
        System.out.println("4. Book Tickets");
        System.out.println("5. Cancel Booking");
        System.out.println("6. View Booking History");
        System.out.println("7. Back to Main Menu");
        System.out.println("==================================================");
    }

    private static void printAdminMenu() {
        System.out.println("\n# ==================================================");
        System.out.println("ADMINISTRATOR MENU");
        System.out.println("1. Add Movie");
        System.out.println("2. View Movies");
        System.out.println("3. Update Movie Details");
        System.out.println("4. Remove Movie");
        System.out.println("5. Manage Show Timings");
        System.out.println("6. View All Bookings");
        System.out.println("7. View Revenue Report");
        System.out.println("8. View Seat Occupancy");
        System.out.println("9. Back to Main Menu");
        System.out.println("==================================================");
    }

    private static void viewSeatAvailability(Theatre theatre) throws SQLException {
        theatre.displayShows();
        int showId = readInt("Enter show ID to view seat map: ");
        Show show = theatre.findShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Invalid show ID.");
        }
        show.displaySeatMap();
    }

    private static void bookTickets(Theatre theatre) throws SQLException {
        theatre.displayShows();
        int showId = readInt("Enter show ID: ");
        Show show = theatre.findShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Invalid show ID.");
        }

        show.displaySeatMap();
        String seatInput = readNonEmptyInput("Enter seats separated by commas (example: C3,C4): ");
        List<String> seatCodes = parseSeatCodes(seatInput);

        String couponCode = readOptionalInput("Enter coupon code (press Enter to skip): ");
        Booking booking = show.bookSeats(seatCodes, couponCode);

        System.out.println("\n# ==================================================");
        System.out.println("BOOKING SUMMARY");
        System.out.printf("Booking ID : %d%n", booking.getBookingId());
        System.out.printf("Movie      : %s%n", booking.getMovieName());
        System.out.printf("Show       : %s %s%n", booking.getShowDate(), booking.getShowTime());
        System.out.printf("Seats      : %s%n", String.join(", ", booking.getSeatCodes()));
        System.out.printf("Total      : Rs. %.2f%n", booking.getTotalAmount());
        if (couponCode != null && !couponCode.isBlank()) {
            System.out.printf("Coupon     : %s%n", couponCode);
        }
        System.out.println("Status     : CONFIRMED");
        System.out.println("Booking successful!");
        System.out.println("==================================================");
    }

    private static void cancelBooking(Theatre theatre) throws SQLException {
        int showId = readInt("Enter show ID: ");
        Show show = theatre.findShow(showId);
        if (show == null) {
            throw new IllegalArgumentException("Invalid show ID.");
        }

        int bookingId = readInt("Enter booking ID: ");
        double refund = show.cancelBooking(bookingId);
        System.out.printf("\nBooking cancelled successfully. Refund amount: Rs. %.2f%n", refund);
    }

    private static void addMovie(Theatre theatre) throws SQLException {
        String movieName = readNonEmptyInput("Enter movie name: ");
        String genre = readNonEmptyInput("Enter genre: ");
        int duration = readInt("Enter duration in minutes: ");
        theatre.addMovie(movieName, genre, duration);
        System.out.println("Movie added successfully.");
    }

    private static void updateMovie(Theatre theatre) throws SQLException {
        theatre.displayMovies();
        int movieId = readInt("Enter movie ID to update: ");
        String movieName = readNonEmptyInput("Enter new movie name: ");
        String genre = readNonEmptyInput("Enter new genre: ");
        int duration = readInt("Enter new duration in minutes: ");
        theatre.updateMovie(movieId, movieName, genre, duration);
        System.out.println("Movie updated successfully.");
    }

    private static void removeMovie(Theatre theatre) throws SQLException {
        theatre.displayMovies();
        int movieId = readInt("Enter movie ID to remove: ");
        theatre.deleteMovie(movieId);
        System.out.println("Movie removed successfully.");
    }

    private static void manageShowTimings(Theatre theatre) throws SQLException {
        theatre.displayShows();
        System.out.println("1. Add show");
        System.out.println("2. Update show");
        System.out.println("3. Delete show");
        System.out.println("4. Back");
        String action = readNonEmptyInput("Choose an option: ");

        switch (action) {
            case "1":
                theatre.displayMovies();
                int movieId = readInt("Enter movie ID: ");
                String showDate = readNonEmptyInput("Enter show date (YYYY-MM-DD): ");
                String showTime = readNonEmptyInput("Enter show time (HH:MM:SS): ");
                theatre.addShow(movieId, showDate, showTime);
                System.out.println("Show added successfully.");
                break;
            case "2":
                int showId = readInt("Enter show ID to update: ");
                theatre.displayMovies();
                int updatedMovieId = readInt("Enter new movie ID: ");
                String updatedDate = readNonEmptyInput("Enter new show date (YYYY-MM-DD): ");
                String updatedTime = readNonEmptyInput("Enter new show time (HH:MM:SS): ");
                theatre.updateShow(showId, updatedMovieId, updatedDate, updatedTime);
                System.out.println("Show updated successfully.");
                break;
            case "3":
                int deleteShowId = readInt("Enter show ID to delete: ");
                theatre.deleteShow(deleteShowId);
                System.out.println("Show deleted successfully.");
                break;
            case "4":
                return;
            default:
                System.out.println("Invalid selection.");
        }
    }

    private static List<String> parseSeatCodes(String input) {
        String[] codes = input.split(",");
        List<String> seatCodes = new ArrayList<>();
        for (String code : codes) {
            String seat = code.trim().toUpperCase();
            if (!seat.matches("^[A-E][1-8]$")) {
                throw new IllegalArgumentException("Invalid seat code: " + code.trim());
            }
            if (seatCodes.contains(seat)) {
                throw new IllegalArgumentException("Duplicate seat code: " + seat);
            }
            seatCodes.add(seat);
        }
        return seatCodes;
    }

    private static int readInt(String prompt) {
        while (true) {
            String value = readNonEmptyInput(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static String readOptionalInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
