package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class OutForDeliveryState implements OrderState {

    static final OutForDeliveryState INSTANCE = new OutForDeliveryState();

    private OutForDeliveryState() {
    }

    @Override
    public void markDelivered(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.OUT_FOR_DELIVERY;
    }
}
