package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tz.ac.dit.parking.exception.AppException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

/** Base type for anything that can occupy a parking slot. */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "vehicle")
public abstract class Vehicle {

    /** Tanzanian civilian plate: T + 3 digits + 3 letters, e.g. T123ABC */
    private static final Pattern PLATE_PATTERN = Pattern.compile("^T[0-9]{3}[A-Z]{3}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", nullable = false, unique = true, length = 7)
    private String plateNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    protected Vehicle() {
    }

    protected Vehicle(String plateNumber, User owner) {
        setPlateNumber(plateNumber);
        setOwner(owner);
    }

    /** POLYMORPHISM: each subclass prices itself. */
    public abstract BigDecimal hourlyRate();

    /** POLYMORPHISM: each subclass knows how much space it needs. */
    public abstract SlotSize requiredSize();

    /**
     * ENCAPSULATION: the only way to write the plate.
     * Trims, uppercases, and rejects invalid formats.
     */
    public void setPlateNumber(String rawPlate) {
        if (rawPlate == null) {
            throw AppException.badRequest("Invalid plate number: null. Expected format T123ABC.");
        }
        String normalized = rawPlate.trim().toUpperCase(Locale.ROOT);
        if (!PLATE_PATTERN.matcher(normalized).matches()) {
            throw AppException.badRequest(
                    "Invalid plate number: '" + rawPlate + "'. Expected format T123ABC.");
        }
        this.plateNumber = normalized;
    }

    public void setOwner(Customer owner) {
        if (owner == null) {
            throw AppException.badRequest("Vehicle owner is required.");
        }
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public User getOwner() {
        return owner;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
