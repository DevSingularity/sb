package org.example;

import org.example.Notification.EmailService;
import org.example.Notification.NotificationService;
import org.example.Notification.SmsService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main() {
        System.out.println("Hello World!");
//        OrderService order = new OrderService();

        NotificationService notif = new SmsService();
        OrderService order = new OrderService(notif);
        order.placeOrder();
    }
}
// a class should ask what it needs, not build everything itself

// IoC- inversion of control (Idea/Principle hai)
// dependency inversion is a way/approach/technique to achieve IoC

// Spring framework has IOC Container, which creates, manages and connects objects together.