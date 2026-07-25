package tz.ac.dit.parking.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.ac.dit.parking.config.AuthenticatedUserProvider;
import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.service.ParkingService;
import tz.ac.dit.parking.web.dto.CheckInRequest;
import tz.ac.dit.parking.web.dto.SessionResponse;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final ParkingService parkingService;
    private final AuthenticatedUserProvider currentUser;

    public SessionController(ParkingService parkingService, AuthenticatedUserProvider currentUser) {
        this.parkingService = parkingService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<SessionResponse> list() {
        return parkingService.findVisibleTo(currentUser.current()).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/check-in")
    public SessionResponse checkIn(@RequestBody CheckInRequest request) {
        return toResponse(parkingService.checkIn(currentUser.current(), request));
    }

    @PostMapping("/{id}/check-out")
    public SessionResponse checkOut(@PathVariable Long id) {
        return toResponse(parkingService.checkOut(currentUser.current(), id));
    }

    private SessionResponse toResponse(ParkingSession session) {
        return SessionResponse.from(session, parkingService.ticketCodeFor(session));
    }
}
