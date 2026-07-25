package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.parking.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
