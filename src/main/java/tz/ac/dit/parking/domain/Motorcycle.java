package tz.ac.dit.parking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "motorcycle")
public class Motorcycle extends Vehicle {

    private static final BigDecimal HOURLY_RATE = new BigDecimal("1000");

    @Column(name = "has_sidecar", nullable = false)
    private boolean sidecar;

    protected Motorcycle() {
    }

    public Motorcycle(String plateNumber, User owner, boolean sidecar) {
        super(plateNumber, owner);
        this.sidecar = sidecar;
    }

    @Override
    public BigDecimal hourlyRate() {
        return HOURLY_RATE;
    }

    @Override
    public SlotSize requiredSize() {
        return SlotSize.SMALL;
    }

    public boolean hasSidecar() {
        return sidecar;
    }
}
