package tournament_trail.demo.fixtures.dtos;

import tournament_trail.demo.web.dtos.PaymentRequest;

public class PaymentRequestFixture {
    public static PaymentRequest create(){
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentReference("TEST");

        return paymentRequest;
    }
}
