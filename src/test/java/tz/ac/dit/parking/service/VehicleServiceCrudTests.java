package tz.ac.dit.parking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.domain.Vehicle;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.CustomerRepository;
import tz.ac.dit.parking.repository.ParkingSessionRepository;
import tz.ac.dit.parking.repository.VehicleRepository;
import tz.ac.dit.parking.web.dto.UpdateVehicleRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceCrudTests {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void updateAllowsOwnerToEditPlateAndVehicleDetailsWhenNotInUse() {
        Customer owner = new Customer("juma", "hash", "Juma", "071", false);
        Car car = new Car("T123ABC", owner, 4);
        UpdateVehicleRequest request = new UpdateVehicleRequest("T777XYZ", null, 2, null, null);

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(car));
        when(vehicleRepository.findByPlateNumber("T777XYZ")).thenReturn(Optional.empty());
        when(sessionRepository.existsByVehicleAndStatusNot(car, SessionStatus.CLOSED)).thenReturn(false);

        Vehicle updated = vehicleService.update(owner, 10L, request);

        assertThat(updated.getPlateNumber()).isEqualTo("T777XYZ");
        assertThat(((Car) updated).getDoors()).isEqualTo(2);
    }

    @Test
    void deleteRemovesVehicleWithoutParkingHistory() {
        Customer owner = new Customer("neema", "hash", "Neema", "072", true);
        Car car = new Car("T123ABC", owner, 4);

        when(vehicleRepository.findById(12L)).thenReturn(Optional.of(car));
        when(sessionRepository.existsByVehicle(car)).thenReturn(false);

        vehicleService.delete(owner, 12L);

        verify(vehicleRepository).delete(car);
    }

    @Test
    void deleteRejectsVehicleWithParkingHistory() {
        Customer owner = new Customer("juma", "hash", "Juma", "071", false);
        Car car = new Car("T123ABC", owner, 4);

        when(vehicleRepository.findById(12L)).thenReturn(Optional.of(car));
        when(sessionRepository.existsByVehicle(car)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.delete(owner, 12L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("parking history");
    }
}
