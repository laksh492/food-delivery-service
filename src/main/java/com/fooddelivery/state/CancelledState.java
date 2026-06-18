package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;

public final class CancelledState implements OrderState {

    static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
