package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.Motorcycle;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.domain.Truck;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.CustomerRepository;
import tz.ac.dit.parking.repository.ParkingSessionRepository;
import tz.ac.dit.parking.repository.VehicleRepository;
import tz.ac.dit.parking.web.dto.RegisterVehicleRequest;
import tz.ac.dit.parking.web.dto.UpdateVehicleRequest;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final ParkingSessionRepository sessionRepository;

    public VehicleService(VehicleRepository vehicleRepository, CustomerRepository customerRepository,
                          ParkingSessionRepository sessionRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.sessionRepository = sessionRepository;
    }

    public Vehicle register(User actor, RegisterVehicleRequest request) {
        Customer owner = resolveOwner(actor, request.ownerId());
        return vehicleRepository.save(build(request, owner));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findVisibleTo(User actor) {
        if (actor instanceof Customer customer) {
            return vehicleRepository.findByOwner(customer);
        }
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle requireByPlate(String plateNumber) {
        String plate = plateNumber.trim().toUpperCase(Locale.ROOT);
        return vehicleRepository.findByPlateNumber(plate)
                .orElseThrow(() -> AppException.notFound("No vehicle found for '" + plateNumber + "'."));
    }

    public Vehicle update(User actor, Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = requireById(id);
        requireCanManage(actor, vehicle);

        if (sessionRepository.existsByVehicleAndStatusNot(vehicle, SessionStatus.CLOSED)) {
            throw AppException.conflict("Cannot update vehicle while it has an active or unpaid session.");
        }

        updatePlate(vehicle, request.plateNumber());
        updateOwner(actor, vehicle, request.ownerId());
        updateDetails(vehicle, request);

        return vehicle;
    }

    public void delete(User actor, Long id) {
        Vehicle vehicle = requireById(id);
        requireCanManage(actor, vehicle);

        if (sessionRepository.existsByVehicle(vehicle)) {
            throw AppException.conflict("Cannot delete vehicle with parking history. Keep it for reports.");
        }

        vehicleRepository.delete(vehicle);
    }

    private Vehicle requireById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("No vehicle with id " + id));
    }

    private void requireCanManage(User actor, Vehicle vehicle) {
        if (!actor.canOperate(vehicle)) {
            throw AppException.forbidden("You can only manage your own vehicles.");
        }
    }

    private void updatePlate(Vehicle vehicle, String rawPlate) {
        if (rawPlate == null || rawPlate.isBlank()) {
            return;
        }

        String plate = rawPlate.trim().toUpperCase(Locale.ROOT);
        if (plate.equals(vehicle.getPlateNumber())) {
            return;
        }

        vehicleRepository.findByPlateNumber(plate)
                .filter(existing -> existing != vehicle
                        && (existing.getId() == null || vehicle.getId() == null
                        || !existing.getId().equals(vehicle.getId())))
                .ifPresent(existing -> {
                    throw AppException.conflict("Vehicle plate " + plate + " is already registered.");
                });
        vehicle.setPlateNumber(plate);
    }

    private void updateOwner(User actor, Vehicle vehicle, Long ownerId) {
        if (ownerId == null) {
            return;
        }
        if (actor instanceof Customer customer) {
            if (!ownerId.equals(customer.getId())) {
                throw AppException.forbidden("You can only keep vehicles assigned to yourself.");
            }
            return;
        }
        Customer owner = customerRepository.findById(ownerId)
                .orElseThrow(() -> AppException.notFound("No customer with id " + ownerId));
        vehicle.setOwner(owner);
    }

    private void updateDetails(Vehicle vehicle, UpdateVehicleRequest request) {
        if (vehicle instanceof Car car && request.doors() != null) {
            car.setDoors(request.doors());
        } else if (vehicle instanceof Truck truck && request.axleCount() != null) {
            truck.setAxleCount(request.axleCount());
        } else if (vehicle instanceof Motorcycle motorcycle && request.sidecar() != null) {
            motorcycle.setSidecar(request.sidecar());
        }
    }

    private Customer resolveOwner(User actor, Long requestedOwnerId) {
        if (actor instanceof Customer customer) {
            if (requestedOwnerId != null && !requestedOwnerId.equals(customer.getId())) {
                throw AppException.forbidden("You can only register vehicles for yourself.");
            }
            return customer;
        }
        if (requestedOwnerId == null) {
            throw AppException.badRequest("An admin must say which customer owns the vehicle.");
        }
        return customerRepository.findById(requestedOwnerId)
                .orElseThrow(() -> AppException.notFound("No customer with id " + requestedOwnerId));
    }

    private Vehicle build(RegisterVehicleRequest request, Customer owner) {
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
