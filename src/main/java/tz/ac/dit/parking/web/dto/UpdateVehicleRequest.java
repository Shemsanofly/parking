package tz.ac.dit.parking.web.dto;

public record UpdateVehicleRequest(String plateNumber, Long ownerId,
                                   Integer doors, Integer axleCount, Boolean sidecar) {
}
