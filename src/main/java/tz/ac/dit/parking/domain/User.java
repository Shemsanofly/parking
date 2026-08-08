package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Everyone who can log in — admins and customers alike — lives in this one table.
 * ENCAPSULATION: role is chosen by a factory method and has no setter,
 * so a customer cannot quietly become an admin.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Customers only — lets them use DISABLED bays. */
    @Column(name = "disability_permit", nullable = false)
    private boolean disabilityPermit;

    /** Admins only. */
    @Column(name = "staff_number", length = 20)
    private String staffNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected User() {
    }

    private User(String username, String passwordHash, String fullName, String phone, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    public static User admin(String username, String passwordHash, String fullName,
                             String phone, String staffNumber) {
        User user = new User(username, passwordHash, fullName, phone, Role.ADMIN);
        user.staffNumber = staffNumber;
        return user;
    }

    public static User customer(String username, String passwordHash, String fullName,
                                String phone, boolean disabilityPermit) {
        User user = new User(username, passwordHash, fullName, phone, Role.CUSTOMER);
        user.disabilityPermit = disabilityPermit;
        return user;
    }

    /** Keeps if (admin) / if (customer) branching out of the services. */
    public boolean canOperate(Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }
        return isAdmin() || this.equals(vehicle.getOwner());
    }

    public String roleName() {
        return role.name();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean hasDisabilityPermit() {
        return disabilityPermit;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
