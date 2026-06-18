package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class PendingPaymentState implements OrderState {

    static final PendingPaymentState INSTANCE = new PendingPaymentState();

    private PendingPaymentState() {
    }

    @Override
    public void markPaymentSuccess(Order order) {
        order.setStatus(OrderStatus.PLACED);
    }

    @Override
    public void markPaymentFailed(Order order) {
        order.setStatus(OrderStatus.PAYMENT_FAILED);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING_PAYMENT;
    }
}
