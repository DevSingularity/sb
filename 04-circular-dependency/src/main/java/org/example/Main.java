package org.example;

//import org.example.simple.A;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    static void main() {
//        $1
//        A a = new A();
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService order = context.getBean(OrderService.class);
        OrderService order2 = context.getBean(OrderService.class);

        if (order == order2) System.out.println("Same");
//        order.placeOrder();
    }
}
