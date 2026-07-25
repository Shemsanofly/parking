package tz.ac.dit.parking.domain;

public record PaymentResult(boolean successful, String reference, String message) {

    public static PaymentResult success(String reference, String message) {
        return new PaymentResult(true, reference, message);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}
