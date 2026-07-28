package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.domain.Vehicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findByVehicleAndStatusNot(Vehicle vehicle, SessionStatus status);
    boolean existsByVehicle(Vehicle vehicle);
    boolean existsByVehicleAndStatusNot(Vehicle vehicle, SessionStatus status);
    boolean existsBySlot(ParkingSlot slot);
    List<ParkingSession> findByVehicleOwnerOrderByEntryTimeDesc(Customer owner);
    List<ParkingSession> findAllByOrderByEntryTimeDesc();

    @Query("""
            select coalesce(sum(s.fee), 0)
            from ParkingSession s
            where s.status = tz.ac.dit.parking.domain.SessionStatus.CLOSED
              and s.exitTime between :from and :to
            """)
    BigDecimal totalRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    long countByStatusNot(SessionStatus status);
}
