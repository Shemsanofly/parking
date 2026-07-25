package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer extends User {

    @Column(name = "disability_permit", nullable = false)
    private boolean disabilityPermit;

    protected Customer() {
    }

    public Customer(String username, String passwordHash, String fullName, String phone, boolean disabilityPermit) {
        super(username, passwordHash, fullName, phone);
        this.disabilityPermit = disabilityPermit;
    }

    @Override
    public String roleName() {
        return "CUSTOMER";
    }

    @Override
    public boolean canOperate(Vehicle vehicle) {
        return vehicle != null && this.equals(vehicle.getOwner());
    }

    public boolean hasDisabilityPermit() {
        return disabilityPermit;
    }
}
