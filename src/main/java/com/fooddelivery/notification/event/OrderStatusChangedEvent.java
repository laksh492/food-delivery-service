package com.fooddelivery.notification.event;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderStatusChangedEvent {

    Integer orderId;
    Integer customerId;
    Integer restaurantId;
    Integer assignedPartnerId;
    City city;
    OrderStatus previousStatus;
    OrderStatus newStatus;
}
