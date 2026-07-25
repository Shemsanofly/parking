package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "mobile_money_payment")
public class MobileMoneyPayment extends Payment {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MobileMoneyProvider provider;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(name = "transaction_ref", length = 20)
    private String transactionRef;

    protected MobileMoneyPayment() {
    }

    public MobileMoneyPayment(ParkingSession session, BigDecimal amount,
                              MobileMoneyProvider provider, String msisdn) {
        super(session, amount);
        this.provider = provider;
        this.msisdn = msisdn;
    }

    @Override
    public PaymentResult process() {
        this.transactionRef = "MM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        markSuccessful();
        return PaymentResult.success(transactionRef, provider + " confirmed for " + msisdn + ".");
    }

    public MobileMoneyProvider getProvider() { return provider; }
    public String getMsisdn() { return msisdn; }
    public String getTransactionRef() { return transactionRef; }
}
