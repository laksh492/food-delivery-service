package com.fooddelivery.notification;

public interface NotificationChannel {

    void send(String recipientType, Integer recipientId, String message);
}
