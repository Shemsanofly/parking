package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ABSTRACTION: the service holds a Payment and calls process().
 * It never learns whether money arrived as cash, card, or mobile money.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "payment")
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ParkingSession session;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    protected Payment() {
    }

    protected Payment(ParkingSession session, BigDecimal amount) {
        this.session = session;
        this.amount = amount;
    }

    public abstract PaymentResult process();

    protected void markSuccessful() {
        this.status = PaymentStatus.SUCCESSFUL;
        this.paidAt = Instant.now();
    }

    protected void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public Long getId() { return id; }
    public ParkingSession getSession() { return session; }
    public BigDecimal getAmount() { return amount; }
    public Instant getPaidAt() { return paidAt; }
    public PaymentStatus getStatus() { return status; }
}
