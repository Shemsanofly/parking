package tz.ac.dit.parking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "vip_slot")
public class VipSlot extends ParkingSlot {

    /** Null means premium but open to anyone. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_for_id")
    private Customer reservedFor;

    protected VipSlot() {
    }

    public VipSlot(String code, SlotSize size, Customer reservedFor) {
        super(code, size);
        this.reservedFor = reservedFor;
    }

    @Override
    public BigDecimal rateMultiplier() {
        return new BigDecimal("1.5");
    }

    @Override
    public boolean accepts(Vehicle vehicle) {
        if (!sizeFits(vehicle)) {
            return false;
        }
        return reservedFor == null || reservedFor.equals(vehicle.getOwner());
    }

    public Customer getReservedFor() {
        return reservedFor;
    }

    public void reserveFor(Customer customer) {
        this.reservedFor = customer;
    }

    public void clearReservation() {
        this.reservedFor = null;
    }
}
