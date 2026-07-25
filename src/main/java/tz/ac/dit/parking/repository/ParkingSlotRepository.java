package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.parking.domain.ParkingSlot;

import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    Optional<ParkingSlot> findByCode(String code);
    List<ParkingSlot> findByOccupiedFalseOrderByCodeAsc();
    List<ParkingSlot> findAllByOrderByCodeAsc();
    long countByOccupiedTrue();
}
