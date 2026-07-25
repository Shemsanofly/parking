package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.SlotType;

public record CreateSlotRequest(String code, SlotType type, SlotSize size, Long reservedForId) {
}
