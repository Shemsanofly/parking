package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.Customer;
import tz.ac.dit.parking.domain.User;

public record UserResponse(Long id, String username, String fullName, String role,
                           boolean disabilityPermit) {

    public static UserResponse from(User user) {
        boolean permit = user instanceof Customer customer && customer.hasDisabilityPermit();
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.roleName(), permit);
    }
}
