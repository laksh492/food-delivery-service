package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class PlacedState implements OrderState {

    static final PlacedState INSTANCE = new PlacedState();

    private PlacedState() {
    }

    @Override
    public void accept(Order order) {
        order.setStatus(OrderStatus.ACCEPTED);
    }

    @Override
    public void reject(Order order) {
        order.setStatus(OrderStatus.REJECTED);
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PLACED;
    }
}
