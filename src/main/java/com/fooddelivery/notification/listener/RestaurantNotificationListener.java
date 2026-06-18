package com.fooddelivery.notification.listener;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.notification.NotificationChannel;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import com.fooddelivery.repository.RestaurantRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantNotificationListener {

    private static final Set<OrderStatus> RESTAURANT_RELEVANT = Set.of(
            OrderStatus.PLACED,
            OrderStatus.CANCELLED,
            OrderStatus.ACCEPTED,
            OrderStatus.REJECTED,
            OrderStatus.DELIVERED
    );

    private final NotificationChannel notificationChannel;
    private final RestaurantRepository restaurantRepository;

    @Async("notificationExecutor")
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (!RESTAURANT_RELEVANT.contains(event.getNewStatus())) {
            return;
        }
        restaurantRepository.findById(event.getRestaurantId())
                .ifPresent(restaurant -> notifyOwner(restaurant, event));
    }

    private void notifyOwner(Restaurant restaurant, OrderStatusChangedEvent event) {
        String message = String.format("Restaurant order %d is now %s",
                event.getOrderId(), event.getNewStatus());
        notificationChannel.send("RESTAURANT_OWNER", restaurant.getOwnerId(), message);
    }
}
