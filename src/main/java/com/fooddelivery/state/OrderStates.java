package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import java.util.Map;

public final class OrderStates {

    private static final Map<OrderStatus, OrderState> STATES = Map.of(
            OrderStatus.PENDING_PAYMENT, PendingPaymentState.INSTANCE,
            OrderStatus.PLACED, PlacedState.INSTANCE,
            OrderStatus.ACCEPTED, AcceptedState.INSTANCE,
            OrderStatus.PREPARING, PreparingState.INSTANCE,
            OrderStatus.OUT_FOR_DELIVERY, OutForDeliveryState.INSTANCE,
            OrderStatus.DELIVERED, DeliveredState.INSTANCE,
            OrderStatus.REJECTED, RejectedState.INSTANCE,
            OrderStatus.CANCELLED, CancelledState.INSTANCE,
            OrderStatus.PAYMENT_FAILED, PaymentFailedState.INSTANCE
    );

    private OrderStates() {
    }

    public static OrderState of(OrderStatus status) {
        OrderState state = STATES.get(status);
        if (state == null) {
            throw new IllegalArgumentException("Unknown order status: " + status);
        }
        return state;
    }
}
