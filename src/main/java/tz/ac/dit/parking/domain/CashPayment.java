package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "cash_payment")
public class CashPayment extends Payment {

    @Column(name = "amount_tendered", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTendered;

    @Column(name = "change_given", precision = 12, scale = 2)
    private BigDecimal changeGiven;

    protected CashPayment() {
    }

    public CashPayment(ParkingSession session, BigDecimal amount, BigDecimal amountTendered) {
        super(session, amount);
        this.amountTendered = amountTendered;
    }

    @Override
    public PaymentResult process() {
        if (amountTendered.compareTo(getAmount()) < 0) {
            markFailed();
            return PaymentResult.failure(
                    "Tendered " + amountTendered + " is less than the " + getAmount() + " due.");
        }
        this.changeGiven = amountTendered.subtract(getAmount());
        markSuccessful();
        return PaymentResult.success("CASH-" + getSession().getId(), "Change due: " + changeGiven);
    }

    public BigDecimal getAmountTendered() { return amountTendered; }
    public BigDecimal getChangeGiven() { return changeGiven; }
}
