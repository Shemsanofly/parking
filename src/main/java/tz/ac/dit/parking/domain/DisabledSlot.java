package tz.ac.dit.parking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "disabled_slot")
public class DisabledSlot extends ParkingSlot {

    protected DisabledSlot() {
    }

    public DisabledSlot(String code, SlotSize size) {
        super(code, size);
    }

    @Override
    public BigDecimal rateMultiplier() {
        return new BigDecimal("1.0");
    }

    @Override
    public boolean accepts(Vehicle vehicle) {
        return sizeFits(vehicle) && vehicle.getOwner().hasDisabilityPermit();
    }
}
