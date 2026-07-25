package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.MobileMoneyProvider;
import tz.ac.dit.parking.domain.PaymentMethod;

import java.math.BigDecimal;

public record PayRequest(Long sessionId, PaymentMethod method, BigDecimal amountTendered,
                         String cardLast4, MobileMoneyProvider provider, String msisdn) {
}
