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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/** One vehicle stay in one slot, from check-in to payment. */
@Entity
@Table(name = "parking_session")
public class ParkingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkingSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(precision = 12, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    protected ParkingSession() {
    }

    public ParkingSession(Vehicle vehicle, ParkingSlot slot, User operator, Instant entryTime) {
        this.vehicle = vehicle;
        this.slot = slot;
        this.operator = operator;
        this.entryTime = entryTime;
        this.status = SessionStatus.ACTIVE;
    }

    /**
     * POLYMORPHISM showcase — one expression, no if/switch/cast:
     * vehicle prices itself, slot applies its multiplier.
     */
    public BigDecimal calculateFee() {
        return vehicle.hourlyRate()
                .multiply(slot.rateMultiplier())
                .multiply(BigDecimal.valueOf(billableHours()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Partial hours round up; every stay bills at least one hour. */
    public long billableHours() {
        Instant end = exitTime != null ? exitTime : Instant.now();
        long minutes = Duration.between(entryTime, end).toMinutes();
        return Math.max(1, (minutes + 59) / 60);
    }

    /** ENCAPSULATION: exit time and fee are set together here. */
    public void close(Instant exit) {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session " + id + " is " + status + ", not ACTIVE.");
        }
        if (exit.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time.");
        }
        this.exitTime = exit;
        this.fee = calculateFee();
        this.status = SessionStatus.AWAITING_PAYMENT;
    }

    public void markPaid() {
        if (status != SessionStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Session " + id + " is " + status + ", not AWAITING_PAYMENT.");
        }
        this.status = SessionStatus.CLOSED;
    }

    public Long getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSlot getSlot() { return slot; }
    public User getOperator() { return operator; }
    public Instant getEntryTime() { return entryTime; }
    public Instant getExitTime() { return exitTime; }
    public BigDecimal getFee() { return fee; }
    public SessionStatus getStatus() { return status; }
}
