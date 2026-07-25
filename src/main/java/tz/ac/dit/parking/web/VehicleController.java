package tz.ac.dit.parking.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.ac.dit.parking.config.AuthenticatedUserProvider;
import tz.ac.dit.parking.service.VehicleService;
import tz.ac.dit.parking.web.dto.RegisterVehicleRequest;
import tz.ac.dit.parking.web.dto.VehicleResponse;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthenticatedUserProvider currentUser;

    public VehicleController(VehicleService vehicleService, AuthenticatedUserProvider currentUser) {
        this.vehicleService = vehicleService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<VehicleResponse> list() {
        return vehicleService.findVisibleTo(currentUser.current()).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @PostMapping
    public VehicleResponse register(@RequestBody RegisterVehicleRequest request) {
        return VehicleResponse.from(vehicleService.register(currentUser.current(), request));
    }
}
