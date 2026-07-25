package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.repository.ParkingSessionRepository;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.web.dto.OccupancyReport;
import tz.ac.dit.parking.web.dto.RevenueReport;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ParkingSessionRepository sessionRepository;
    private final ParkingSlotRepository slotRepository;

    public ReportService(ParkingSessionRepository sessionRepository,
                         ParkingSlotRepository slotRepository) {
        this.sessionRepository = sessionRepository;
        this.slotRepository = slotRepository;
    }

    public RevenueReport revenue(Instant from, Instant to) {
        BigDecimal total = sessionRepository.totalRevenueBetween(from, to);
        long closed = sessionRepository.findAllByOrderByEntryTimeDesc().stream()
                .filter(s -> s.getStatus() == SessionStatus.CLOSED)
                .filter(s -> s.getExitTime() != null
                        && !s.getExitTime().isBefore(from)
                        && !s.getExitTime().isAfter(to))
                .count();
        return new RevenueReport(from, to, total, closed);
    }

    public OccupancyReport occupancy() {
        long total = slotRepository.count();
        long occupied = slotRepository.countByOccupiedTrue();
        long free = total - occupied;
        int percent = total == 0 ? 0 : (int) Math.round((occupied * 100.0) / total);
        long onSite = sessionRepository.countByStatusNot(SessionStatus.CLOSED);
        return new OccupancyReport(total, occupied, free, percent, onSite);
    }
}
