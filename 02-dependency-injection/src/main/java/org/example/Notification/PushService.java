package org.example.Notification;

public class PushService implements NotificationService {
    @Override
    public void sendNotif() {
        System.out.println("Push Notif sent");
    }
}
