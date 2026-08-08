package tz.ac.dit.parking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Motorcycle;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.SlotType;
import tz.ac.dit.parking.domain.Truck;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.repository.UserRepository;
import tz.ac.dit.parking.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;

/** First-run demo data. Skips if admin already exists. */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(UserRepository userRepository,
                          VehicleRepository vehicleRepository,
                          ParkingSlotRepository slotRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("shemsa")) {
            return;
        }

        String password = passwordEncoder.encode("password");

        userRepository.save(User.admin("shemsa", password, "Shemsa Amin", "0754000001", "STF-001"));

        User juma = userRepository.save(
                User.customer("juma", password, "Juma Ally", "0754000002", false));
        User neema = userRepository.save(
                User.customer("neema", password, "Neema Paul", "0754000003", true));

        vehicleRepository.save(new Car("T123ABC", juma, 4));
        vehicleRepository.save(new Truck("T124ABC", juma, 3));
        vehicleRepository.save(new Motorcycle("T125ABC", neema, false));
        vehicleRepository.save(new Car("T126ABC", neema, 2));

        slotRepository.saveAll(buildSlots(neema));
    }

    private List<ParkingSlot> buildSlots(User vipHolder) {
        List<ParkingSlot> slots = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            slots.add(new ParkingSlot(String.format("A-%02d", i), SlotType.STANDARD, SlotSize.MEDIUM));
        }
        for (int i = 7; i <= 8; i++) {
            slots.add(new ParkingSlot(String.format("A-%02d", i), SlotType.STANDARD, SlotSize.SMALL));
        }
        for (int i = 9; i <= 10; i++) {
            slots.add(new ParkingSlot(String.format("A-%02d", i), SlotType.STANDARD, SlotSize.LARGE));
        }
        for (int i = 1; i <= 5; i++) {
            slots.add(new ParkingSlot(String.format("B-%02d", i), SlotType.STANDARD, SlotSize.MEDIUM));
        }

        slots.add(new ParkingSlot("V-01", SlotType.VIP, SlotSize.LARGE));
        slots.add(new ParkingSlot("V-02", SlotType.VIP, SlotSize.MEDIUM));
        slots.add(new ParkingSlot("V-03", SlotType.VIP, SlotSize.MEDIUM, vipHolder));

        slots.add(new ParkingSlot("D-01", SlotType.DISABLED, SlotSize.MEDIUM));
        slots.add(new ParkingSlot("D-02", SlotType.DISABLED, SlotSize.MEDIUM));

        return slots;
    }
}
