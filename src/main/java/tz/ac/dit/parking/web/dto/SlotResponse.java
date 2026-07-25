package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.DisabledSlot;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.SlotType;
import tz.ac.dit.parking.domain.VipSlot;

import java.math.BigDecimal;

public record SlotResponse(Long id, String code, SlotType type, SlotSize size,
                           boolean occupied, BigDecimal rateMultiplier, String reservedFor) {

    public static SlotResponse from(ParkingSlot slot) {
        SlotType type;
        String reservedFor = null;
        if (slot instanceof VipSlot vip) {
            type = SlotType.VIP;
            reservedFor = vip.getReservedFor() == null ? null : vip.getReservedFor().getFullName();
        } else if (slot instanceof DisabledSlot) {
            type = SlotType.DISABLED;
        } else {
            type = SlotType.STANDARD;
        }
        return new SlotResponse(slot.getId(), slot.getCode(), type, slot.getSize(),
                slot.isOccupied(), slot.rateMultiplier(), reservedFor);
    }
}
