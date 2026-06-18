package com.fooddelivery.notification.listener;

import com.fooddelivery.notification.NotificationChannel;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerNotificationListener {

    private final NotificationChannel notificationChannel;

    @Async("notificationExecutor")
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String message = String.format("Order %d status changed from %s to %s",
                event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
        notificationChannel.send("CUSTOMER", event.getCustomerId(), message);
    }
}
