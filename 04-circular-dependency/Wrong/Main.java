package org.example.Wrong;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WMain {
    static void main() {
        ApplicationContext context = new AnnotationConfigApplicationContext(WAppConfig.class);

        WOrderService order = context.getBean(WOrderService.class);
        order.placeOrder();

    }
}
