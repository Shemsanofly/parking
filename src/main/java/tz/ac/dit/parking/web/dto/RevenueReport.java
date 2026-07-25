package tz.ac.dit.parking.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueReport(Instant from, Instant to, BigDecimal total, long closedSessions) {
}
