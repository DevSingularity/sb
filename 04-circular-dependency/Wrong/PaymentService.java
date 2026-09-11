package org.example.Wrong;

import org.springframework.stereotype.Component;

@Component
public class WPaymentService {

    private WOrderService orderService;

    public WPaymentService(WOrderService orderService) {
        this.orderService = orderService;
    }

    public void pay() {
        orderService.getDetails();
        System.out.println("Payment done");
    }
}
