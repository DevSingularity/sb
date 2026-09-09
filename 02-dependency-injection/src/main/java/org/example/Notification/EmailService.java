package org.example.Notification;

public class EmailService implements NotificationService {
    @Override
    public void sendNotif() {
        System.out.println("Email sent");
    }
}
