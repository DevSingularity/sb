package org.example.payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component

//$2
// this qualifier will be identified by default name "upiPayment"
@Qualifier
public class UpiPayment implements PaymentService {
    @Override
    public void pay() {
        System.out.println("Payment done with UPI");
    }
}
