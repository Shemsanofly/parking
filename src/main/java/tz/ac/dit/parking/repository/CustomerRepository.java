package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.parking.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
