package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.domain.Ticket;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findBySession(ParkingSession session);
}
