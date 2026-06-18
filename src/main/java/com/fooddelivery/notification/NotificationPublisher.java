package com.fooddelivery.notification;

import com.fooddelivery.notification.event.AssignmentOfferedEvent;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        eventPublisher.publishEvent(event);
    }

    public void publishAssignmentOffered(AssignmentOfferedEvent event) {
        eventPublisher.publishEvent(event);
    }
}
