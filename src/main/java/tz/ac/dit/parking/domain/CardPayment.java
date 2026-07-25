package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "card_payment")
public class CardPayment extends Payment {

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "authorization_code", length = 20)
    private String authorizationCode;

    protected CardPayment() {
    }

    public CardPayment(ParkingSession session, BigDecimal amount, String cardLast4) {
        super(session, amount);
        this.cardLast4 = cardLast4;
    }

    /** Simulated: cards ending in 0 always decline (easy to demo). */
    @Override
    public PaymentResult process() {
        if (cardLast4.endsWith("0")) {
            markFailed();
            return PaymentResult.failure("Card ending " + cardLast4 + " was declined by the issuer.");
        }
        this.authorizationCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        markSuccessful();
        return PaymentResult.success(authorizationCode, "Approved.");
    }

    public String getCardLast4() { return cardLast4; }
    public String getAuthorizationCode() { return authorizationCode; }
}
