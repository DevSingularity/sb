package org.example.Notification;

public class SmsService implements NotificationService {
    @Override
    public void sendNotif() {
        System.out.println("SMS sent");
    }
}
