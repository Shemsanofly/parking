package tz.ac.dit.parking.domain;

/** Physical size: a slot fits a vehicle when its size is at least as large. */
public enum SlotSize {
    SMALL, MEDIUM, LARGE;

    public boolean fits(SlotSize required) {
        return this.ordinal() >= required.ordinal();
    }
}
