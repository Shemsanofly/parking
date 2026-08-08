package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Motorcycle;
import tz.ac.dit.parking.domain.Role;
import tz.ac.dit.parking.domain.Truck;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.UserRepository;
import tz.ac.dit.parking.repository.VehicleRepository;
import tz.ac.dit.parking.web.dto.RegisterVehicleRequest;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    public Vehicle register(User actor, RegisterVehicleRequest request) {
        User owner = resolveOwner(actor, request.ownerId());
        return vehicleRepository.save(build(request, owner));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findVisibleTo(User actor) {
        if (actor.isCustomer()) {
            return vehicleRepository.findByOwner(actor);
        }
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle requireByPlate(String plateNumber) {
        String plate = plateNumber.trim().toUpperCase(Locale.ROOT);
        return vehicleRepository.findByPlateNumber(plate)
                .orElseThrow(() -> AppException.notFound("No vehicle found for '" + plateNumber + "'."));
    }

    private User resolveOwner(User actor, Long requestedOwnerId) {
        if (actor.isCustomer()) {
            if (requestedOwnerId != null && !requestedOwnerId.equals(actor.getId())) {
                throw AppException.forbidden("You can only register vehicles for yourself.");
            }
            return actor;
        }
        if (requestedOwnerId == null) {
            throw AppException.badRequest("An admin must say which customer owns the vehicle.");
        }
        return userRepository.findByIdAndRole(requestedOwnerId, Role.CUSTOMER)
                .orElseThrow(() -> AppException.notFound("No customer with id " + requestedOwnerId));
    }

    private Vehicle build(RegisterVehicleRequest request, User owner) {
        return switch (request.type()) {
            case CAR -> new Car(request.plateNumber(), owner,
                    request.doors() == null ? 4 : request.doors());
            case TRUCK -> new Truck(request.plateNumber(), owner,
                    request.axleCount() == null ? 2 : request.axleCount());
            case MOTORCYCLE -> new Motorcycle(request.plateNumber(), owner,
                    Boolean.TRUE.equals(request.sidecar()));
        };
    }
}
