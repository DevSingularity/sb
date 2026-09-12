package org.example;

import org.springframework.stereotype.Component;

@Component
public class OrderService {
    private PaymentService ps;

    public OrderService(PaymentService ps) {
        this.ps = ps;
    }

    public void placeOrder() {
        ps.pay();
        System.out.println("Order placed");
    }
}
