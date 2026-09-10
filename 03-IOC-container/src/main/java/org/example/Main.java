package org.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        //Old: $0
        //OrderService orderService = new OrderService();

//        New: $1.0
//        OrderService order = context.getBean(OrderService.class);
//        order.placeOrder();
//        PaymentService payment = context.getBean(PaymentService.class);
//        payment.pay();

//        $1.1
//        Using AutoWired:
        OrderService order = context.getBean(OrderService.class);
        order.placeOrder();

//        $3
        User user = context.getBean(User.class);
        System.out.println(user.getName());
        System.out.println(user.getAge());

//        $3- problem
//        ProductService ps = new ProductService();
//        ps.productProp();

//        $3.2- solution
        ProductService ps = context.getBean(ProductService.class);
        ps.productProp();
    }
}
