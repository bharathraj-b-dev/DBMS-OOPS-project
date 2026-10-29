import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CineApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String ADMIN_PASSWORD = "admin123";
    private static final int MENU_WIDTH = 50;

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
        printMenuLine();
        printCentered("CINEBOOK");
        printCentered("MOVIE BOOKING SYSTEM");
        printMenuOptions("1. Customer", "2. Administrator", "3. Exit");
        printMenuLine();
    }

    private static void printCustomerMenu() {
        printMenuLine();
        printCentered("CUSTOMER MENU");
        printMenuOptions(
                "1. View Movies",
                "2. View Show Timings",
                "3. View Seat Availability",
                "4. Book Tickets",
                "5. Cancel Booking",
                "6. View Booking History",
                "7. Back to Main Menu"
        );
        printMenuLine();
    }

    private static void printAdminMenu() {
        printMenuLine();
        printCentered("ADMINISTRATOR MENU");
        printMenuOptions(
                "1. Add Movie",
                "2. View Movies",
                "3. Update Movie Details",
                "4. Remove Movie",
                "5. Manage Show Timings",
                "6. View All Bookings",
                "7. View Revenue Report",
                "8. View Seat Occupancy",
                "9. Back to Main Menu"
        );
        printMenuLine();
    }

    private static void printMenuLine() {
        System.out.println("\n" + "=".repeat(MENU_WIDTH));
    }

    private static void printCentered(String text) {
        int leftPadding = Math.max(0, (MENU_WIDTH - text.length()) / 2);
        System.out.printf("%" + leftPadding + "s%s%n", "", text);
    }

    private static void printMenuOptions(String... options) {
        int longestOption = 0;
        for (String option : options) {
            longestOption = Math.max(longestOption, option.length());
        }

        int leftPadding = Math.max(0, (MENU_WIDTH - longestOption) / 2);
        for (String option : options) {
            System.out.printf("%" + leftPadding + "s%s%n", "", option);
        }
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

    private interface Bookable {
        Booking bookSeats(List<String> seatCodes, String couponCode) throws SQLException;
        double cancelBooking(int bookingId) throws SQLException;
    }

    private static class Booking {
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

    private static class DBConnection {
        private static final String URL = System.getProperty(
                "db.url",
                "jdbc:mysql://localhost:3306/cinebook?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        private static final String USER = System.getProperty("db.user", "root");
        private static final String PASSWORD = System.getProperty("db.password", "2504");

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    private static class Movie {
        private final int movieId;
        private final String movieName;
        private final String genre;
        private final int durationMinutes;

        public Movie(int movieId, String movieName, String genre, int durationMinutes) {
            this.movieId = movieId;
            this.movieName = movieName;
            this.genre = genre;
            this.durationMinutes = durationMinutes;
        }

        public int getMovieId() {
            return movieId;
        }

        public String getMovieName() {
            return movieName;
        }

        public String getGenre() {
            return genre;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }
    }

    private enum SeatType {
        REGULAR(150),
        PREMIUM(250),
        VIP(400);

        private final double price;

        SeatType(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }

        public static SeatType fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Seat type cannot be null.");
            }
            return SeatType.valueOf(value.trim().toUpperCase());
        }

        public static SeatType fromSeatCode(String seatCode) {
            if (seatCode == null || seatCode.isBlank()) {
                throw new IllegalArgumentException("Seat code cannot be empty.");
            }

            char row = Character.toUpperCase(seatCode.trim().charAt(0));
            if (row >= 'A' && row <= 'B') {
                return REGULAR;
            }
            if (row >= 'C' && row <= 'D') {
                return PREMIUM;
            }
            if (row == 'E') {
                return VIP;
            }
            throw new IllegalArgumentException("Invalid seat code: " + seatCode);
        }
    }

    private static class Show implements Bookable {
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

    private static class Theatre {
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
}
