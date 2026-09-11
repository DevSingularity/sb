package org.example;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

//$1
//@Component
//public class PaymentService {
//
////    private OrderService orderService;
//
////    public PaymentService(OrderService orderService) {
////        this.orderService = orderService;
////    }
//
//    public void pay() {
//
////        Not the responsibility of PaymentService- $1 solution
////        orderService.getDetails();
//        System.out.println("Payment done");
//    }
//}

@Component
@Lazy //$3
public class PaymentService {
    public void pay() {
        System.out.println("Payment done");
    }
}