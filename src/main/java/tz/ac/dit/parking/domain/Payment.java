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
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * One attempt to settle a session's fee. Cash, card and mobile money share this
 * table; {@link PaymentMethod} decides which of the optional columns are filled
 * and which branch {@link #process()} runs.
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ParkingSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** CASH only. */
    @Column(name = "amount_tendered", precision = 12, scale = 2)
    private BigDecimal amountTendered;

    /** CASH only. */
    @Column(name = "change_given", precision = 12, scale = 2)
    private BigDecimal changeGiven;

    /** CARD only. */
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    /** MOBILE_MONEY only. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MobileMoneyProvider provider;

    /** MOBILE_MONEY only. */
    @Column(length = 15)
    private String msisdn;

    /** Authorization code, transaction ref, or cash receipt number. */
    @Column(length = 30)
    private String reference;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    protected Payment() {
    }

    private Payment(ParkingSession session, PaymentMethod method, BigDecimal amount) {
        this.session = session;
        this.method = method;
        this.amount = amount;
    }

    public static Payment cash(ParkingSession session, BigDecimal amount, BigDecimal amountTendered) {
        Payment payment = new Payment(session, PaymentMethod.CASH, amount);
        payment.amountTendered = amountTendered;
        return payment;
    }

    public static Payment card(ParkingSession session, BigDecimal amount, String cardLast4) {
        Payment payment = new Payment(session, PaymentMethod.CARD, amount);
        payment.cardLast4 = cardLast4;
        return payment;
    }

    public static Payment mobileMoney(ParkingSession session, BigDecimal amount,
                                      MobileMoneyProvider provider, String msisdn) {
        Payment payment = new Payment(session, PaymentMethod.MOBILE_MONEY, amount);
        payment.provider = provider;
        payment.msisdn = msisdn;
        return payment;
    }

    /** Runs the simulated gateway for whichever method this payment uses. */
    public PaymentResult process() {
        return switch (method) {
            case CASH -> processCash();
            case CARD -> processCard();
            case MOBILE_MONEY -> processMobileMoney();
        };
    }

    private PaymentResult processCash() {
        if (amountTendered == null || amountTendered.compareTo(amount) < 0) {
            markFailed();
            return PaymentResult.failure(
                    "Tendered " + amountTendered + " is less than the " + amount + " due.");
        }
        this.changeGiven = amountTendered.subtract(amount);
        this.reference = "CASH-" + session.getId();
        markSuccessful();
        return PaymentResult.success(reference, "Change due: " + changeGiven);
    }

    /** Simulated: cards ending in 0 always decline (easy to demo). */
    private PaymentResult processCard() {
        if (cardLast4 == null || cardLast4.endsWith("0")) {
            markFailed();
            return PaymentResult.failure("Card ending " + cardLast4 + " was declined by the issuer.");
        }
        this.reference = "AUTH-" + shortId();
        markSuccessful();
        return PaymentResult.success(reference, "Approved.");
    }

    private PaymentResult processMobileMoney() {
        this.reference = "MM-" + shortId();
        markSuccessful();
        return PaymentResult.success(reference, provider + " confirmed for " + msisdn + ".");
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private void markSuccessful() {
        this.status = PaymentStatus.SUCCESSFUL;
        this.paidAt = Instant.now();
    }

    private void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public Long getId() { return id; }
    public ParkingSession getSession() { return session; }
    public PaymentMethod getMethod() { return method; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getAmountTendered() { return amountTendered; }
    public BigDecimal getChangeGiven() { return changeGiven; }
    public String getCardLast4() { return cardLast4; }
    public MobileMoneyProvider getProvider() { return provider; }
    public String getMsisdn() { return msisdn; }
    public String getReference() { return reference; }
    public Instant getPaidAt() { return paidAt; }
    public PaymentStatus getStatus() { return status; }
}
