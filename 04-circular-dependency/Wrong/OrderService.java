package org.example.Wrong;

import org.springframework.stereotype.Component;

@Component
public class WOrderService {

    private WPaymentService paymentService;

    public WOrderService(WPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void placeOrder() {
        paymentService.pay();
        System.out.println("Order placed");
    }

    public void getDetails() {
        System.out.println("Getting details");
    }
}
