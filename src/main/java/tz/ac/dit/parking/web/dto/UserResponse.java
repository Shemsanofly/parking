package tz.ac.dit.parking.web.dto;

import tz.ac.dit.parking.domain.Role;
import tz.ac.dit.parking.domain.User;

public record UserResponse(Long id, String username, String fullName, Role role,
                           boolean disabilityPermit) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getRole(), user.hasDisabilityPermit());
    }
}
