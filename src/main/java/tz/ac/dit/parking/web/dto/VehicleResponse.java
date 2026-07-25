package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Motorcycle;
import tz.ac.dit.parking.domain.Truck;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.domain.VehicleType;

import java.math.BigDecimal;

public record VehicleResponse(Long id, String plateNumber, VehicleType type, String ownerName,
                              Long ownerId, BigDecimal hourlyRate, String detail) {

    public static VehicleResponse from(Vehicle vehicle) {
        VehicleType type;
        String detail;
        if (vehicle instanceof Car car) {
            type = VehicleType.CAR;
            detail = car.getDoors() + " doors";
        } else if (vehicle instanceof Truck truck) {
            type = VehicleType.TRUCK;
            detail = truck.getAxleCount() + " axles";
        } else {
            Motorcycle motorcycle = (Motorcycle) vehicle;
            type = VehicleType.MOTORCYCLE;
            detail = motorcycle.hasSidecar() ? "with sidecar" : "no sidecar";
        }
        return new VehicleResponse(vehicle.getId(), vehicle.getPlateNumber(), type,
                vehicle.getOwner().getFullName(), vehicle.getOwner().getId(),
                vehicle.hourlyRate(), detail);
    }
}
