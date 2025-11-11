package luxdine.example.luxdine.domain.reservation.enums;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    ARRIVED,
    CANCELLED;

    public static ReservationStatus fromNullable(String name) {
        if (name == null) return null;
        try { return ReservationStatus.valueOf(name); }
        catch (IllegalArgumentException ex) { return null; }
    }

}
