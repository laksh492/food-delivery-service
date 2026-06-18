package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class AcceptedState implements OrderState {

    static final AcceptedState INSTANCE = new AcceptedState();

    private AcceptedState() {
    }

    @Override
    public void startPreparing(Order order) {
        order.setStatus(OrderStatus.PREPARING);
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ACCEPTED;
    }
}
