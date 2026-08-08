package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.Payment;
import tz.ac.dit.parking.domain.PaymentMethod;
import tz.ac.dit.parking.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, Long sessionId, PaymentMethod method, BigDecimal amount,
                              BigDecimal changeGiven, String reference, PaymentStatus status,
                              Instant paidAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getSession().getId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getChangeGiven(),
                payment.getReference(),
                payment.getStatus(),
                payment.getPaidAt());
    }
}
