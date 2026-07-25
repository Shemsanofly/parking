package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.VehicleType;

public record RegisterVehicleRequest(VehicleType type, String plateNumber, Long ownerId,
                                     Integer doors, Integer axleCount, Boolean sidecar) {
}
