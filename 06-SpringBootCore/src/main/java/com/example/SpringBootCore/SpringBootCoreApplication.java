package com.example.SpringBootCore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootCoreApplication {
	public static void main(String[] args) {
		ApplicationContext context =
				SpringApplication.run(SpringBootCoreApplication.class, args);

//		PaymentGateway paymentGateway = context.getBean(PaymentGateway.class);
//
//		paymentGateway.print();
	}

}