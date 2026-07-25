package tz.ac.dit.parking.web.dto;

/** slotCode null = auto-assign first free accepting slot */
public record CheckInRequest(String plateNumber, String slotCode) {
}
