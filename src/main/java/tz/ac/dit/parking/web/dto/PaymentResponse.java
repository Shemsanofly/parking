package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.CardPayment;
import tz.ac.dit.parking.domain.CashPayment;
import tz.ac.dit.parking.domain.MobileMoneyPayment;
import tz.ac.dit.parking.domain.Payment;
import tz.ac.dit.parking.domain.PaymentMethod;
import tz.ac.dit.parking.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, Long sessionId, PaymentMethod method, BigDecimal amount,
                              BigDecimal changeGiven, String reference, PaymentStatus status,
                              Instant paidAt) {

    public static PaymentResponse from(Payment payment) {
        PaymentMethod method;
        BigDecimal change = null;
        String reference;
        if (payment instanceof CashPayment cash) {
            method = PaymentMethod.CASH;
            change = cash.getChangeGiven();
            reference = "CASH-" + payment.getSession().getId();
        } else if (payment instanceof CardPayment card) {
            method = PaymentMethod.CARD;
            reference = card.getAuthorizationCode();
        } else {
            MobileMoneyPayment mobile = (MobileMoneyPayment) payment;
            method = PaymentMethod.MOBILE_MONEY;
            reference = mobile.getTransactionRef();
        }
        return new PaymentResponse(payment.getId(), payment.getSession().getId(), method,
                payment.getAmount(), change, reference, payment.getStatus(), payment.getPaidAt());
    }
}
