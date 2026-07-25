package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.DisabledSlot;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.StandardSlot;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.domain.VipSlot;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.CustomerRepository;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.web.dto.CreateSlotRequest;
import tz.ac.dit.parking.web.dto.UpdateSlotRequest;

import java.util.List;

@Service
@Transactional
public class SlotService {

    private final ParkingSlotRepository slotRepository;
    private final CustomerRepository customerRepository;

    public SlotService(ParkingSlotRepository slotRepository, CustomerRepository customerRepository) {
        this.slotRepository = slotRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<ParkingSlot> findAll() {
        return slotRepository.findAllByOrderByCodeAsc();
    }

    public ParkingSlot create(CreateSlotRequest request) {
        Customer reservedFor = request.reservedForId() == null ? null
                : customerRepository.findById(request.reservedForId())
                        .orElseThrow(() -> AppException.notFound(
                                "No customer with id " + request.reservedForId()));

        ParkingSlot slot = switch (request.type()) {
            case STANDARD -> new StandardSlot(request.code(), request.size());
            case VIP -> new VipSlot(request.code(), request.size(), reservedFor);
            case DISABLED -> new DisabledSlot(request.code(), request.size());
        };
        return slotRepository.save(slot);
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

        if (slot instanceof VipSlot vip) {
            if (request.clearReservation()) {
                vip.clearReservation();
            } else if (request.reservedForId() != null) {
                Customer holder = customerRepository.findById(request.reservedForId())
                        .orElseThrow(() -> AppException.notFound(
                                "No customer with id " + request.reservedForId()));
                vip.reserveFor(holder);
            }
        }

        return slot;
    }

    @Transactional(readOnly = true)
    public ParkingSlot firstAvailableFor(Vehicle vehicle) {
        return slotRepository.findByOccupiedFalseOrderByCodeAsc().stream()
                .filter(slot -> slot.accepts(vehicle))
                .findFirst()
                .orElseThrow(() -> AppException.conflict(
                        "No free slot accepts vehicle " + vehicle.getPlateNumber()));
    }
}
