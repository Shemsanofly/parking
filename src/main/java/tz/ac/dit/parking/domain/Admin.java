package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
public class Admin extends User {

    @Column(name = "staff_number", nullable = false, length = 20)
    private String staffNumber;

    protected Admin() {
    }

    public Admin(String username, String passwordHash, String fullName, String phone, String staffNumber) {
        super(username, passwordHash, fullName, phone);
        this.staffNumber = staffNumber;
    }

    @Override
    public String roleName() {
        return "ADMIN";
    }

    @Override
    public boolean canOperate(Vehicle vehicle) {
        return vehicle != null;
    }

    public String getStaffNumber() {
        return staffNumber;
    }
}
