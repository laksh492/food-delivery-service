package com.fooddelivery.notification.listener;

import com.fooddelivery.notification.NotificationChannel;
import com.fooddelivery.notification.event.AssignmentOfferedEvent;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryPartnerNotificationListener {

    private final NotificationChannel notificationChannel;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Async("notificationExecutor")
    @EventListener
    public void onAssignmentOffered(AssignmentOfferedEvent event) {
        deliveryPartnerRepository.findAvailableByCity(event.getCity()).forEach(partner -> {
            String message = String.format("New delivery assignment available for order %d", event.getOrderId());
            notificationChannel.send("DELIVERY_PARTNER", partner.getUserId(), message);
        });
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getAssignedPartnerId() == null) {
            return;
        }
        deliveryPartnerRepository.findById(event.getAssignedPartnerId()).ifPresent(partner -> {
            String message = String.format("Assigned order %d status changed from %s to %s",
                    event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
            notificationChannel.send("DELIVERY_PARTNER", partner.getUserId(), message);
        });
    }
}
