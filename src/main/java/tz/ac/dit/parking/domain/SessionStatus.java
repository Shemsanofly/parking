package tz.ac.dit.parking.domain;

public enum SessionStatus {
    ACTIVE,
    AWAITING_PAYMENT,
    CLOSED;

    public boolean isUnclosed() {
        return this != CLOSED;
    }
}
