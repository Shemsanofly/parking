package tz.ac.dit.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tz.ac.dit.parking.domain.CardPayment;
import tz.ac.dit.parking.domain.CashPayment;
import tz.ac.dit.parking.domain.MobileMoneyPayment;
import tz.ac.dit.parking.domain.ParkingSession;
import tz.ac.dit.parking.domain.Payment;
import tz.ac.dit.parking.domain.PaymentResult;
import tz.ac.dit.parking.domain.SessionStatus;
import tz.ac.dit.parking.domain.User;
import tz.ac.dit.parking.exception.AppException;
import tz.ac.dit.parking.repository.PaymentRepository;
import tz.ac.dit.parking.web.dto.PayRequest;

import java.math.BigDecimal;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ParkingService parkingService;

    public PaymentService(PaymentRepository paymentRepository, ParkingService parkingService) {
        this.paymentRepository = paymentRepository;
        this.parkingService = parkingService;
    }

    public Payment pay(User actor, PayRequest request) {
        ParkingSession session = parkingService.requireSession(request.sessionId());
        parkingService.requireCanOperate(actor, session.getVehicle());

        if (session.getStatus() != SessionStatus.AWAITING_PAYMENT) {
            throw AppException.conflict("Parking session " + session.getId() + " is not awaiting payment.");
        }

        Payment payment = build(request, session, session.getFee());

        // ABSTRACTION: this line does not know which payment method it is running.
        PaymentResult result = payment.process();

        if (!result.successful()) {
            throw AppException.paymentFailed(result.message());
        }

        paymentRepository.save(payment);
        session.markPaid();
        session.getSlot().release();
        return payment;
    }

    private Payment build(PayRequest request, ParkingSession session, BigDecimal amountDue) {
        return switch (request.method()) {
            case CASH -> new CashPayment(session, amountDue, request.amountTendered());
            case CARD -> new CardPayment(session, amountDue, request.cardLast4());
            case MOBILE_MONEY -> new MobileMoneyPayment(session, amountDue,
                    request.provider(), request.msisdn());
        };
    }
}
