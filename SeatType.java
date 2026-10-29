public enum SeatType {
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
