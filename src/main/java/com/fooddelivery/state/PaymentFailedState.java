package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;

public final class PaymentFailedState implements OrderState {

    static final PaymentFailedState INSTANCE = new PaymentFailedState();

    private PaymentFailedState() {
    }

    @Override
    public void retryPayment(Order order) {
        order.setStatus(OrderStatus.PENDING_PAYMENT);
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PAYMENT_FAILED;
    }
}
