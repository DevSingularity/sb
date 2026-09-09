package org.example;

import org.example.Notification.NotificationService;

public class OrderService {
//    before:
//    EmailService notif = new EmailService();

    // after:
    NotificationService notif;

    public OrderService(NotificationService notif) {
        this.notif = notif;
    }

    public void placeOrder() {
        System.out.println("Order has been placed!");
        notif.sendNotif();
    }
}
