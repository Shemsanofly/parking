package tz.ac.dit.parking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.StandardSlot;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.CustomerRepository;
import tz.ac.dit.parking.repository.ParkingSessionRepository;
import tz.ac.dit.parking.repository.ParkingSlotRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceCrudTests {

    @Mock
    private ParkingSlotRepository slotRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    @InjectMocks
    private SlotService slotService;

    @Test
    void deleteRemovesFreeSlotWithoutParkingHistory() {
        ParkingSlot slot = new StandardSlot("A01", SlotSize.MEDIUM);

        when(slotRepository.findByCode("A01")).thenReturn(Optional.of(slot));
        when(sessionRepository.existsBySlot(slot)).thenReturn(false);

        slotService.delete("A01");

        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteRejectsOccupiedSlot() {
        ParkingSlot slot = new StandardSlot("A01", SlotSize.MEDIUM);
        slot.occupy();

        when(slotRepository.findByCode("A01")).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> slotService.delete("A01"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("occupied");
    }
}
