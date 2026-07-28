package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "truck")
public class Truck extends Vehicle {

    private static final BigDecimal HOURLY_RATE = new BigDecimal("5000");

    @Column(name = "axle_count", nullable = false)
    private int axleCount;

    protected Truck() {
    }

    public Truck(String plateNumber, Customer owner, int axleCount) {
        super(plateNumber, owner);
        setAxleCount(axleCount);
    }

    @Override
    public BigDecimal hourlyRate() {
        return HOURLY_RATE;
    }

    @Override
    public SlotSize requiredSize() {
        return SlotSize.LARGE;
    }

    public int getAxleCount() {
        return axleCount;
    }

    public void setAxleCount(int axleCount) {
        if (axleCount < 2 || axleCount > 8) {
            throw new IllegalArgumentException("Truck axles must be between 2 and 8.");
        }
        this.axleCount = axleCount;
    }
}
