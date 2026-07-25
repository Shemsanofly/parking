package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.domain.SessionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionResponse(Long id, String plateNumber, String slotCode, String operatorName,
                              Instant entryTime, Instant exitTime, BigDecimal fee,
                              SessionStatus status, String ticketCode) {

    public static SessionResponse from(ParkingSession session, String ticketCode) {
        return new SessionResponse(
                session.getId(),
                session.getVehicle().getPlateNumber(),
                session.getSlot().getCode(),
                session.getOperator().getFullName(),
                session.getEntryTime(),
                session.getExitTime(),
                session.getFee(),
                session.getStatus(),
                ticketCode);
    }
}
