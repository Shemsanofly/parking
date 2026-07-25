package tz.ac.dit.parking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tz.ac.dit.parking.domain.Admin;
import tz.ac.dit.parking.domain.Car;
import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.DisabledSlot;
import tz.ac.dit.parking.domain.Motorcycle;
import tz.ac.dit.parking.domain.ParkingSlot;
import tz.ac.dit.parking.domain.SlotSize;
import tz.ac.dit.parking.domain.StandardSlot;
import tz.ac.dit.parking.domain.Truck;
import tz.ac.dit.parking.domain.VipSlot;
import tz.ac.dit.parking.repository.CustomerRepository;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.repository.UserRepository;
import tz.ac.dit.parking.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;

/** First-run demo data. Skips if admin already exists. */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataRunner(UserRepository userRepository,
                          CustomerRepository customerRepository,
                          VehicleRepository vehicleRepository,
                          ParkingSlotRepository slotRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
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

        userRepository.save(new Admin("shemsa", password, "Shemsa Amin", "0754000001", "STF-001"));

        Customer juma = customerRepository.save(
                new Customer("juma", password, "Juma Ally", "0754000002", false));
        Customer neema = customerRepository.save(
                new Customer("neema", password, "Neema Paul", "0754000003", true));

        vehicleRepository.save(new Car("T123ABC", juma, 4));
        vehicleRepository.save(new Truck("T124ABC", juma, 3));
        vehicleRepository.save(new Motorcycle("T125ABC", neema, false));
        vehicleRepository.save(new Car("T126ABC", neema, 2));

        slotRepository.saveAll(buildSlots(neema));
    }

    private List<ParkingSlot> buildSlots(Customer vipHolder) {
        List<ParkingSlot> slots = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            slots.add(new StandardSlot(String.format("A-%02d", i), SlotSize.MEDIUM));
        }
        for (int i = 7; i <= 8; i++) {
            slots.add(new StandardSlot(String.format("A-%02d", i), SlotSize.SMALL));
        }
        for (int i = 9; i <= 10; i++) {
            slots.add(new StandardSlot(String.format("A-%02d", i), SlotSize.LARGE));
        }
        for (int i = 1; i <= 5; i++) {
            slots.add(new StandardSlot(String.format("B-%02d", i), SlotSize.MEDIUM));
        }

        slots.add(new VipSlot("V-01", SlotSize.LARGE, null));
        slots.add(new VipSlot("V-02", SlotSize.MEDIUM, null));
        slots.add(new VipSlot("V-03", SlotSize.MEDIUM, vipHolder));

        slots.add(new DisabledSlot("D-01", SlotSize.MEDIUM));
        slots.add(new DisabledSlot("D-02", SlotSize.MEDIUM));

        return slots;
    }
}
