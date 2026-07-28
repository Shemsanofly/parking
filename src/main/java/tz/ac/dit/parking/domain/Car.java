package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "car")
public class Car extends Vehicle {

    private static final BigDecimal HOURLY_RATE = new BigDecimal("2000");

    @Column(nullable = false)
    private int doors;

    protected Car() {
    }

    public Car(String plateNumber, Customer owner, int doors) {
        super(plateNumber, owner);
        setDoors(doors);
    }

    @Override
    public BigDecimal hourlyRate() {
        return HOURLY_RATE;
    }

    @Override
    public SlotSize requiredSize() {
        return SlotSize.MEDIUM;
    }

    public int getDoors() {
        return doors;
    }

    public void setDoors(int doors) {
        if (doors < 2 || doors > 6) {
            throw new IllegalArgumentException("Car doors must be between 2 and 6.");
        }
        this.doors = doors;
    }
}
