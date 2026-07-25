package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.SlotSize;

public record UpdateSlotRequest(SlotSize size, Long reservedForId, boolean clearReservation) {
}
