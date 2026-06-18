package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;

public final class RejectedState implements OrderState {

    static final RejectedState INSTANCE = new RejectedState();

    private RejectedState() {
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.REJECTED;
    }
}
