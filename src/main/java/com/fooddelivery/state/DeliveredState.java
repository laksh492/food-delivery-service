package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;

public final class DeliveredState implements OrderState {

    static final DeliveredState INSTANCE = new DeliveredState();

    private DeliveredState() {
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
}
