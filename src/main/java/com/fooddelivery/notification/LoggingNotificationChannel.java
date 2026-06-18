package com.fooddelivery.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNotificationChannel implements NotificationChannel {

    @Override
    public void send(String recipientType, Integer recipientId, String message) {
        log.info("[NOTIFICATION] {} {}: {}", recipientType, recipientId, message);
    }
}
