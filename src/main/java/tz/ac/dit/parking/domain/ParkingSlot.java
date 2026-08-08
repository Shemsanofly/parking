package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tz.ac.dit.parking.exception.AppException;

import java.math.BigDecimal;

/**
 * One bay in the lot. Standard, VIP and disabled bays are the same table —
 * they differ only by {@link SlotType}, which drives price and who may park.
 */
@Entity
@Table(name = "parking_slot")
public class ParkingSlot {

    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal BASE_MULTIPLIER = new BigDecimal("1.0");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SlotType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SlotSize size;

    @Column(nullable = false)
    private boolean available;

    /** VIP bays only. Null means premium but open to anyone. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_for_id")
    private User reservedFor;

    protected ParkingSlot() {
    }

    public ParkingSlot(String code, SlotType type, SlotSize size) {
        this(code, type, size, null);
    }

    public ParkingSlot(String code, SlotType type, SlotSize size, User reservedFor) {
        this.code = code;
        this.type = type;
        this.size = size;
        this.available = true;
        if (reservedFor != null) {
            reserveFor(reservedFor);
        }
    }

    /** VIP costs more. */
    public BigDecimal rateMultiplier() {
        return type == SlotType.VIP ? VIP_MULTIPLIER : BASE_MULTIPLIER;
    }

    /** Each slot type decides who may park in it. */
    public boolean accepts(Vehicle vehicle) {
        if (!sizeFits(vehicle)) {
            return false;
        }
        return switch (type) {
            case STANDARD -> true;
            case VIP -> reservedFor == null || reservedFor.equals(vehicle.getOwner());
            case DISABLED -> vehicle.getOwner().hasDisabilityPermit();
        };
    }

    /**
     * ENCAPSULATION: available has no setter.
     * Only occupy() / release() can change it, so a slot cannot be silently double-booked.
     */
    public void occupy() {
        if (!available) {
            throw AppException.conflict("Slot " + code + " is unavailable: already occupied");
        }
        this.available = false;
    }

    public void release() {
        this.available = true;
    }

    public void resize(SlotSize newSize) {
        if (!available) {
            throw AppException.conflict("Slot " + code + " is unavailable: cannot be resized while occupied");
        }
        this.size = newSize;
    }

    public void reserveFor(User customer) {
        if (type != SlotType.VIP) {
            throw AppException.conflict("Slot " + code + " is " + type + "; only VIP bays can be reserved.");
        }
        if (customer != null && !customer.isCustomer()) {
            throw AppException.badRequest("Only customers can hold a VIP reservation.");
        }
        this.reservedFor = customer;
    }

    public void clearReservation() {
        this.reservedFor = null;
    }

    private boolean sizeFits(Vehicle vehicle) {
        return size.fits(vehicle.requiredSize());
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public SlotType getType() {
        return type;
    }

    public SlotSize getSize() {
        return size;
    }

    public boolean isAvailable() {
        return available;
    }

    public User getReservedFor() {
        return reservedFor;
    }
}
