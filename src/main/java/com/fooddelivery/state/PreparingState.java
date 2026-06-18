package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class PreparingState implements OrderState {

    static final PreparingState INSTANCE = new PreparingState();

    private PreparingState() {
    }

    @Override
    public void markOutForDelivery(Order order) {
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PREPARING;
    }
}
