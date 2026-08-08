package tz.ac.dit.parking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Fails if the Spring context cannot start — a wiring/mapping error in any
 * entity, repository, service or security bean shows up here first.
 * Behaviour is covered by {@link ApiSmokeTest}.
 */
@SpringBootTest
class ParkingApplicationTests {

    @Test
    void contextLoads() {
    }
}
