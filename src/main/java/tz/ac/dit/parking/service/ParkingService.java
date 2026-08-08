package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.domain.Ticket;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.ParkingSessionRepository;
import tz.ac.dit.parking.repository.TicketRepository;
import tz.ac.dit.parking.web.dto.CheckInRequest;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ParkingService {

    private final ParkingSessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final VehicleService vehicleService;
    private final SlotService slotService;

    public ParkingService(ParkingSessionRepository sessionRepository,
                          TicketRepository ticketRepository,
                          VehicleService vehicleService,
                          SlotService slotService) {
        this.sessionRepository = sessionRepository;
        this.ticketRepository = ticketRepository;
        this.vehicleService = vehicleService;
        this.slotService = slotService;
    }

    public ParkingSession checkIn(User actor, CheckInRequest request) {
        Vehicle vehicle = vehicleService.requireByPlate(request.plateNumber());
        requireCanOperate(actor, vehicle);
        requireNotAlreadyParked(vehicle);

        ParkingSlot slot = chooseSlot(vehicle, request.slotCode());
        slot.occupy();

        ParkingSession session = sessionRepository.save(
                new ParkingSession(vehicle, slot, actor, Instant.now()));
        ticketRepository.save(new Ticket(session, session.getEntryTime()));
        return session;
    }

    public ParkingSession checkOut(User actor, Long sessionId) {
        ParkingSession session = requireSession(sessionId);
        requireCanOperate(actor, session.getVehicle());
        session.close(Instant.now());
        return session;
    }

    @Transactional(readOnly = true)
    public List<ParkingSession> findVisibleTo(User actor) {
        if (actor.isCustomer()) {
            return sessionRepository.findByVehicleOwnerOrderByEntryTimeDesc(actor);
        }
        return sessionRepository.findAllByOrderByEntryTimeDesc();
    }

    @Transactional(readOnly = true)
    public String ticketCodeFor(ParkingSession session) {
        return ticketRepository.findBySession(session).map(Ticket::getTicketCode).orElse(null);
    }

    @Transactional(readOnly = true)
    public ParkingSession requireSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("No parking session with id " + id + "."));
    }

    public void requireCanOperate(User actor, Vehicle vehicle) {
        if (!actor.canOperate(vehicle)) {
            throw AppException.forbidden(
                    "You are not permitted to operate on vehicle " + vehicle.getPlateNumber() + ".");
        }
    }

    private void requireNotAlreadyParked(Vehicle vehicle) {
        sessionRepository.findByVehicleAndStatusNot(vehicle, SessionStatus.CLOSED)
                .ifPresent(existing -> {
                    throw AppException.conflict(
                            "Vehicle " + vehicle.getPlateNumber()
                                    + " already has an unclosed parking session.");
                });
    }

    private ParkingSlot chooseSlot(Vehicle vehicle, String requestedCode) {
        if (requestedCode == null || requestedCode.isBlank()) {
            return slotService.firstAvailableFor(vehicle);
        }
        ParkingSlot slot = slotService.requireByCode(requestedCode);
        if (!slot.isAvailable()) {
            throw AppException.conflict("Slot " + slot.getCode() + " is unavailable: already occupied");
        }
        if (!slot.accepts(vehicle)) {
            throw AppException.conflict(
                    "Slot " + slot.getCode() + " does not accept vehicle " + vehicle.getPlateNumber());
        }
        return slot;
    }
}
