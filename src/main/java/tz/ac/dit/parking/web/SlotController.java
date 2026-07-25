package tz.ac.dit.parking.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.ac.dit.parking.service.SlotService;
import tz.ac.dit.parking.web.dto.CreateSlotRequest;
import tz.ac.dit.parking.web.dto.SlotResponse;
import tz.ac.dit.parking.web.dto.UpdateSlotRequest;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    public List<SlotResponse> list() {
        return slotService.findAll().stream().map(SlotResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SlotResponse create(@RequestBody CreateSlotRequest request) {
        return SlotResponse.from(slotService.create(request));
    }

    @PatchMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public SlotResponse update(@PathVariable String code, @RequestBody UpdateSlotRequest request) {
        return SlotResponse.from(slotService.update(code, request));
    }
}
