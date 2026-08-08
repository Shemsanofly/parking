package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.SlotType;

import java.math.BigDecimal;

public record SlotResponse(Long id, String code, SlotType type, SlotSize size,
                           boolean available, BigDecimal rateMultiplier, String reservedFor) {

    public static SlotResponse from(ParkingSlot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getCode(),
                slot.getType(),
                slot.getSize(),
                slot.isAvailable(),
                slot.rateMultiplier(),
                slot.getReservedFor() == null ? null : slot.getReservedFor().getFullName());
    }
}
