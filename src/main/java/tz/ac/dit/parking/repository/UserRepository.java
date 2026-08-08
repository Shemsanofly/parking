package tz.ac.dit.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tz.ac.dit.parking.domain.Role;
import tz.ac.dit.parking.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByIdAndRole(Long id, Role role);
    boolean existsByUsername(String username);
}
