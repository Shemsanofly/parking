package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import tz.ac.dit.parking.exception.AppException;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "parking_slot")
public abstract class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SlotSize size;

    @Column(nullable = false)
    private boolean occupied;

    protected ParkingSlot() {
    }

    protected ParkingSlot(String code, SlotSize size) {
        this.code = code;
        this.size = size;
        this.occupied = false;
    }

    /** POLYMORPHISM: VIP costs more. */
    public abstract BigDecimal rateMultiplier();

    /** POLYMORPHISM: each slot type decides who may park. */
    public abstract boolean accepts(Vehicle vehicle);

    /**
     * ENCAPSULATION: occupied has no setter.
     * Only occupy() / release() can change it, so a slot cannot be silently double-booked.
     */
    public void occupy() {
        if (occupied) {
            throw AppException.conflict("Slot " + code + " is unavailable: already occupied");
        }
        this.occupied = true;
    }

    public void release() {
        this.occupied = false;
    }

    public void resize(SlotSize newSize) {
        if (occupied) {
            throw AppException.conflict("Slot " + code + " is unavailable: cannot be resized while occupied");
        }
        this.size = newSize;
    }

    protected boolean sizeFits(Vehicle vehicle) {
        return size.fits(vehicle.requiredSize());
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public SlotSize getSize() {
        return size;
    }

    public boolean isOccupied() {
        return occupied;
    }
}
