package org.example.payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

//$1
@Component

//$2
@Qualifier("cp")
public class CardPayment implements PaymentService {
    @Override
    public void pay() {
        System.out.println("Payment done with card");
    }
}
