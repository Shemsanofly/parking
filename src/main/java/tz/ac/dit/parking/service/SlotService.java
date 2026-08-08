package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.Role;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.repository.UserRepository;
import tz.ac.dit.parking.web.dto.CreateSlotRequest;
import tz.ac.dit.parking.web.dto.UpdateSlotRequest;

import java.util.List;

@Service
@Transactional
public class SlotService {

    private final ParkingSlotRepository slotRepository;
    private final UserRepository userRepository;

    public SlotService(ParkingSlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ParkingSlot> findAll() {
        return slotRepository.findAllByOrderByCodeAsc();
    }

    public ParkingSlot create(CreateSlotRequest request) {
        User reservedFor = request.reservedForId() == null ? null
                : requireCustomer(request.reservedForId());
        return slotRepository.save(
                new ParkingSlot(request.code(), request.type(), request.size(), reservedFor));
    }

    @Transactional(readOnly = true)
    public ParkingSlot requireByCode(String code) {
        return slotRepository.findByCode(code)
                .orElseThrow(() -> AppException.notFound("No parking slot found for '" + code + "'."));
    }

    public ParkingSlot update(String code, UpdateSlotRequest request) {
        ParkingSlot slot = requireByCode(code);

        if (request.size() != null && request.size() != slot.getSize()) {
            slot.resize(request.size());
        }

        // reserveFor() rejects non-VIP bays with a 409 rather than ignoring the request.
        if (request.clearReservation()) {
            slot.clearReservation();
        } else if (request.reservedForId() != null) {
            slot.reserveFor(requireCustomer(request.reservedForId()));
        }

        return slot;
    }

    @Transactional(readOnly = true)
    public ParkingSlot firstAvailableFor(Vehicle vehicle) {
        return slotRepository.findByAvailableTrueOrderByCodeAsc().stream()
                .filter(slot -> slot.accepts(vehicle))
                .findFirst()
                .orElseThrow(() -> AppException.conflict(
                        "No free slot accepts vehicle " + vehicle.getPlateNumber()));
    }

    private User requireCustomer(Long id) {
        return userRepository.findByIdAndRole(id, Role.CUSTOMER)
                .orElseThrow(() -> AppException.notFound("No customer with id " + id));
    }
}
