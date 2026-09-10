package org.example;

import org.example.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

//$1
@Component
public class OrderService {

//    Dependency Injection:

//    $1
//    3. Field DI
//    @Autowired
    private PaymentService payment;

//     $1
//     1. Constructor method DI
//    @Autowired
//    public OrderService(PaymentService payment) {
//        this.payment = payment;
//    }

//    $1
//    2. Setter DI
//    @Autowired
//    public void setPayment(PaymentService payment) {
//        this.payment = payment;
//    }



//    $2:
//    2. using the qualifier here....
//    @Autowired
//    public OrderService(@Qualifier("upiPayment") PaymentService payment) {
//        this.payment = payment;
//    }

//    $2
    @Autowired
    public OrderService(@Qualifier("cp") PaymentService payment) {
        this.payment = payment;
    }


//    $3-DI
//    @Autowired
//    public OrderService(PaymentService payment) {
//        this.payment = payment;
//    }

    public void placeOrder() {
        payment.pay();
        System.out.println("Order placed");
    }
}
