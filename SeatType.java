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
        return SeatType.valueOf(value.toUpperCase());
    }
}
