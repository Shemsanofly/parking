package tz.ac.dit.parking.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tz.ac.dit.parking.config.AuthenticatedUserProvider;
import tz.ac.dit.parking.service.PaymentService;
import tz.ac.dit.parking.web.dto.PayRequest;
import tz.ac.dit.parking.web.dto.PaymentResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthenticatedUserProvider currentUser;

    public PaymentController(PaymentService paymentService, AuthenticatedUserProvider currentUser) {
        this.paymentService = paymentService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public PaymentResponse pay(@RequestBody PayRequest request) {
        return PaymentResponse.from(paymentService.pay(currentUser.current(), request));
    }
}
