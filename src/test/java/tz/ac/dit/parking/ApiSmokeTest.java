package tz.ac.dit.parking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.Role;
import tz.ac.dit.parking.repository.ParkingSlotRepository;
import tz.ac.dit.parking.repository.UserRepository;
import tz.ac.dit.parking.repository.VehicleRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * End-to-end smoke tests over the real HTTP layer, security, services and JPA
 * mappings — the same path a browser takes, minus the browser.
 *
 * <p>These lean deliberately on the flattened schema: a user's role, a slot's
 * type/availability and a payment's method are all plain columns now, so the
 * rules that used to live in subclass overrides are what these tests pin down.
 *
 * <p>Each test runs in a transaction that is rolled back afterwards, so the
 * demo data seeded by {@code SeedDataRunner} is identical at every test start.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiSmokeTest {

    private static final String PASSWORD = "password";

    /** Seeded demo vehicles: juma owns a car + truck, neema a motorcycle + car. */
    private static final String JUMA_CAR = "T123ABC";
    private static final String JUMA_TRUCK = "T124ABC";
    private static final String NEEMA_BIKE = "T125ABC";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UserRepository users;
    @Autowired private VehicleRepository vehicles;
    @Autowired private ParkingSlotRepository slots;

    // ------------------------------------------------------------ schema ----

    @Test
    @DisplayName("seed data lands in the flat tables, roles and slot types included")
    void seedDataUsesFlatTables() {
        assertThat(users.count()).isEqualTo(3);
        assertThat(vehicles.count()).isEqualTo(4);
        assertThat(slots.count()).isEqualTo(20);

        assertThat(users.findByUsername("shemsa").orElseThrow().getRole()).isEqualTo(Role.ADMIN);
        assertThat(users.findByUsername("juma").orElseThrow().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(users.findByUsername("neema").orElseThrow().hasDisabilityPermit()).isTrue();

        // The VIP reservation survives as a nullable FK on the single slots table.
        assertThat(slots.findByCode("V-03").orElseThrow().getReservedFor().getUsername())
                .isEqualTo("neema");
    }

    // -------------------------------------------------------------- auth ----

    @Test
    @DisplayName("login reports the role straight from the users.role column")
    void loginReturnsRoleAndPermit() throws Exception {
        JsonNode admin = call(login("shemsa", PASSWORD), 200);
        assertThat(admin.get("role").asText()).isEqualTo("ADMIN");
        assertThat(admin.get("disabilityPermit").asBoolean()).isFalse();

        JsonNode customer = call(login("neema", PASSWORD), 200);
        assertThat(customer.get("role").asText()).isEqualTo("CUSTOMER");
        assertThat(customer.get("disabilityPermit").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("a wrong password is rejected with 401")
    void loginRejectsBadCredentials() throws Exception {
        call(login("juma", "not-the-password"), 401);
    }

    // ------------------------------------------------------------- slots ----

    @Test
    @DisplayName("slot list exposes type, availability and reservation as flat fields")
    void slotListIsFlat() throws Exception {
        JsonNode all = call(get("/api/slots").with(customer("juma")), 200);
        assertThat(all).hasSize(20);

        JsonNode vip = slotNamed(all, "V-03");
        assertThat(vip.get("type").asText()).isEqualTo("VIP");
        assertThat(vip.get("available").asBoolean()).isTrue();
        assertThat(vip.get("reservedFor").asText()).isEqualTo("Neema Paul");
        assertThat(vip.get("rateMultiplier").decimalValue()).isEqualByComparingTo("1.5");

        JsonNode standard = slotNamed(all, "A-01");
        assertThat(standard.get("type").asText()).isEqualTo("STANDARD");
        assertThat(standard.get("reservedFor").isNull()).isTrue();
        assertThat(standard.get("rateMultiplier").decimalValue()).isEqualByComparingTo("1.0");
    }

    @Test
    @DisplayName("a disabled bay turns away a customer with no permit, and admits one with")
    void disabledBayChecksThePermit() throws Exception {
        call(checkIn("juma", JUMA_CAR, "D-01"), 409);

        JsonNode session = call(checkIn("neema", NEEMA_BIKE, "D-01"), 200);
        assertThat(session.get("slotCode").asText()).isEqualTo("D-01");
    }

    @Test
    @DisplayName("a VIP bay reserved for someone else turns other customers away")
    void reservedVipBayIsPrivate() throws Exception {
        call(checkIn("juma", JUMA_CAR, "V-03"), 409);           // reserved for neema
        call(checkIn("juma", JUMA_CAR, "V-01"), 200);           // unreserved VIP is open
    }

    @Test
    @DisplayName("reserving a non-VIP bay is refused rather than silently ignored")
    void onlyVipBaysCanBeReserved() throws Exception {
        long neemaId = users.findByUsername("neema").orElseThrow().getId();

        JsonNode refused = call(reserve("A-01", neemaId), 409);
        assertThat(refused.get("message").asText()).contains("only VIP bays can be reserved");
        call(reserve("D-01", neemaId), 409);

        JsonNode reserved = call(reserve("V-01", neemaId), 200);
        assertThat(reserved.get("reservedFor").asText()).isEqualTo("Neema Paul");
    }

    @Test
    @DisplayName("an occupied bay reports itself unavailable and is freed again by payment")
    void baysFlipAvailabilityAcrossTheCycle() throws Exception {
        long sessionId = call(checkIn("juma", JUMA_CAR, "A-01"), 200).get("id").asLong();
        assertThat(availability("A-01")).isFalse();

        call(checkOut("juma", sessionId), 200);
        assertThat(availability("A-01")).isFalse();             // still held until paid

        call(pay("juma", cashBody(sessionId, "5000")), 200);
        assertThat(availability("A-01")).isTrue();
    }

    // ----------------------------------------------------------- billing ----

    @Test
    @DisplayName("a standard bay bills the vehicle's own rate, a VIP bay bills 1.5x")
    void vipBaysCostHalfAgain() throws Exception {
        long standard = call(checkIn("juma", JUMA_CAR, "A-01"), 200).get("id").asLong();
        assertThat(feeOf(call(checkOut("juma", standard), 200))).isEqualByComparingTo("2000.00");

        long vip = call(checkIn("neema", NEEMA_BIKE, "V-01"), 200).get("id").asLong();
        assertThat(feeOf(call(checkOut("neema", vip), 200))).isEqualByComparingTo("1500.00");
    }

    // ---------------------------------------------------------- payments ----

    @Test
    @DisplayName("cash settles the session and returns change")
    void cashPaymentReturnsChange() throws Exception {
        long id = awaitingPayment("juma", JUMA_CAR, "A-01");

        JsonNode receipt = call(pay("juma", cashBody(id, "5000")), 200);
        assertThat(receipt.get("method").asText()).isEqualTo("CASH");
        assertThat(receipt.get("status").asText()).isEqualTo("SUCCESSFUL");
        assertThat(receipt.get("changeGiven").decimalValue()).isEqualByComparingTo("3000.00");
        assertThat(receipt.get("reference").asText()).isEqualTo("CASH-" + id);
    }

    @Test
    @DisplayName("cash below the fee is declined with 402 and leaves the session open")
    void shortCashIsDeclined() throws Exception {
        long id = awaitingPayment("juma", JUMA_CAR, "A-01");

        JsonNode error = call(pay("juma", cashBody(id, "10")), 402);
        assertThat(error.get("message").asText()).contains("less than the");
        assertThat(statusOf("juma", id)).isEqualTo("AWAITING_PAYMENT");
    }

    @Test
    @DisplayName("a card ending in 0 is declined, any other card is approved")
    void cardEndingInZeroIsDeclined() throws Exception {
        long declined = awaitingPayment("juma", JUMA_CAR, "A-01");
        call(pay("juma", cardBody(declined, "1230")), 402);
        assertThat(statusOf("juma", declined)).isEqualTo("AWAITING_PAYMENT");

        JsonNode receipt = call(pay("juma", cardBody(declined, "4245")), 200);
        assertThat(receipt.get("method").asText()).isEqualTo("CARD");
        assertThat(receipt.get("reference").asText()).startsWith("AUTH-");
        assertThat(receipt.get("changeGiven").isNull()).isTrue();
    }

    @Test
    @DisplayName("mobile money settles and records a transaction reference")
    void mobileMoneySucceeds() throws Exception {
        long id = awaitingPayment("neema", NEEMA_BIKE, "A-07");

        JsonNode receipt = call(pay("neema", """
                {"sessionId":%d,"method":"MOBILE_MONEY","provider":"MPESA","msisdn":"0754000003"}
                """.formatted(id)), 200);
        assertThat(receipt.get("method").asText()).isEqualTo("MOBILE_MONEY");
        assertThat(receipt.get("status").asText()).isEqualTo("SUCCESSFUL");
        assertThat(receipt.get("reference").asText()).startsWith("MM-");
    }

    @Test
    @DisplayName("a settled session cannot be paid twice")
    void payingTwiceIsRefused() throws Exception {
        long id = awaitingPayment("juma", JUMA_CAR, "A-01");

        call(pay("juma", cashBody(id, "5000")), 200);
        call(pay("juma", cashBody(id, "5000")), 409);
    }

    // ------------------------------------------------------ authorisation ----

    @Test
    @DisplayName("a customer cannot touch another customer's vehicle")
    void customersAreScopedToTheirOwnVehicles() throws Exception {
        JsonNode error = call(checkIn("neema", JUMA_TRUCK, null), 403);
        assertThat(error.get("message").asText()).contains(JUMA_TRUCK);

        JsonNode mine = call(get("/api/vehicles").with(customer("juma")), 200);
        assertThat(mine).hasSize(2);
    }

    @Test
    @DisplayName("an admin sees every vehicle and may operate any of them")
    void adminsAreUnscoped() throws Exception {
        JsonNode all = call(get("/api/vehicles").with(admin()), 200);
        assertThat(all).hasSize(4);

        call(post("/api/sessions/check-in").with(admin())
                .contentType(APPLICATION_JSON)
                .content("""
                        {"plateNumber":"%s"}""".formatted(JUMA_TRUCK)), 200);
    }

    @Test
    @DisplayName("reports are admin-only")
    void reportsRequireAdmin() throws Exception {
        call(get("/api/reports/occupancy").with(customer("juma")), 403);

        JsonNode occupancy = call(get("/api/reports/occupancy").with(admin()), 200);
        assertThat(occupancy.get("totalSlots").asLong()).isEqualTo(20);
        assertThat(occupancy.get("freeSlots").asLong()).isEqualTo(20);
    }

    // ----------------------------------------------------------- helpers ----

    /** Drives a request to completion and returns its parsed body. */
    private JsonNode call(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        var response = mvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus())
                .as("%s -> %s", request, response.getContentAsString())
                .isEqualTo(expectedStatus);
        String body = response.getContentAsString();
        return body.isBlank() ? json.nullNode() : json.readTree(body);
    }

    private MockHttpServletRequestBuilder login(String username, String password) {
        return post("/api/auth/login").contentType(APPLICATION_JSON).content("""
                {"username":"%s","password":"%s"}""".formatted(username, password));
    }

    private MockHttpServletRequestBuilder checkIn(String username, String plate, String slotCode) {
        String body = slotCode == null
                ? """
                {"plateNumber":"%s"}""".formatted(plate)
                : """
                {"plateNumber":"%s","slotCode":"%s"}""".formatted(plate, slotCode);
        return post("/api/sessions/check-in").with(customer(username))
                .contentType(APPLICATION_JSON).content(body);
    }

    private MockHttpServletRequestBuilder checkOut(String username, long sessionId) {
        return post("/api/sessions/{id}/check-out", sessionId).with(customer(username));
    }

    private MockHttpServletRequestBuilder pay(String username, String body) {
        return post("/api/payments").with(customer(username))
                .contentType(APPLICATION_JSON).content(body);
    }

    private MockHttpServletRequestBuilder reserve(String slotCode, long customerId) {
        return patch("/api/slots/{code}", slotCode).with(admin())
                .contentType(APPLICATION_JSON).content("""
                        {"reservedForId":%d}""".formatted(customerId));
    }

    private static String cashBody(long sessionId, String tendered) {
        return """
                {"sessionId":%d,"method":"CASH","amountTendered":%s}""".formatted(sessionId, tendered);
    }

    private static String cardBody(long sessionId, String last4) {
        return """
                {"sessionId":%d,"method":"CARD","cardLast4":"%s"}""".formatted(sessionId, last4);
    }

    /** Checks a vehicle in and straight back out, leaving it AWAITING_PAYMENT. */
    private long awaitingPayment(String username, String plate, String slotCode) throws Exception {
        long id = call(checkIn(username, plate, slotCode), 200).get("id").asLong();
        call(checkOut(username, id), 200);
        return id;
    }

    private String statusOf(String username, long sessionId) throws Exception {
        for (JsonNode session : call(get("/api/sessions").with(customer(username)), 200)) {
            if (session.get("id").asLong() == sessionId) {
                return session.get("status").asText();
            }
        }
        throw new AssertionError("No session " + sessionId + " visible to " + username);
    }

    private boolean availability(String slotCode) throws Exception {
        return slotNamed(call(get("/api/slots").with(customer("juma")), 200), slotCode)
                .get("available").asBoolean();
    }

    private static JsonNode slotNamed(JsonNode slots, String code) {
        for (JsonNode slot : slots) {
            if (code.equals(slot.get("code").asText())) {
                return slot;
            }
        }
        throw new AssertionError("No slot " + code + " in response");
    }

    private static BigDecimal feeOf(JsonNode session) {
        return session.get("fee").decimalValue();
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor customer(String username) {
        return user(username).roles("CUSTOMER");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return user("shemsa").roles("ADMIN");
    }
}
