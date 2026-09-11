package org.example;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//$1
//@Component
//public class OrderService {
//
//    private PaymentService paymentService;
//
//    public OrderService(PaymentService paymentService) {
//        this.paymentService = paymentService;
//    }
//
//    public void placeOrder() {
//        paymentService.pay();
//
//        getDetails(); // #1- Solution
//        System.out.println("Order placed");
//    }
//
//    public static void getDetails() {
//        System.out.println("Getting details");
//    }
//}

@Component
//$3
@Scope("prototype")
public class OrderService {
    private PaymentService paymentService;

    //$3- create object of payment service only when this class is used...
    public PaymentService getPaymentService(@Lazy PaymentService paymentService) {
        return paymentService;
    }

    public void placeOrder() {
        System.out.println("Order Service - placeOrder");
    }
}