package org.example;

import org.example.payment.CardPayment;
import org.example.payment.PaymentService;
import org.example.payment.UpiPayment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan("org.example")
public class AppConfig {

    //$3.1
    @Bean
    public User createUser() {
        return new User("Saman", 19);
    }

    //$3.2
    @Bean
    public ProductService createProductService() {
        return new ProductService();
    }

    //$3- only when there is no @Component tag present/used on the classes
//    @Bean
////    $3.4
////    @Primary
//    @Qualifier("cups")
//    public PaymentService createCardPayment() {
//        return new CardPayment();
//    }
//
//    @Bean
//    @Qualifier
//    public PaymentService createUpiPayment() {
//        return new UpiPayment();
//    }
//
//    @Bean
//    public OrderService createOrder(@Qualifier("cups") PaymentService payment) {
//        return new OrderService(payment);
//    }
}
