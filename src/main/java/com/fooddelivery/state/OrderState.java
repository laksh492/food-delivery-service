package com.fooddelivery.state;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.Order;

public interface OrderState {

    default void accept(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.ACCEPTED);
    }

    default void reject(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.REJECTED);
    }

    default void cancel(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.CANCELLED);
    }

    default void markPaymentSuccess(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.PLACED);
    }

    default void markPaymentFailed(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.PAYMENT_FAILED);
    }

    default void retryPayment(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.PENDING_PAYMENT);
    }

    default void startPreparing(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.PREPARING);
    }

    default void markOutForDelivery(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.OUT_FOR_DELIVERY);
    }

    default void markDelivered(Order order) {
        throw invalidTransition(order.getStatus(), OrderStatus.DELIVERED);
    }

    OrderStatus getStatus();

    private static AppException invalidTransition(OrderStatus from, OrderStatus to) {
        return new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                "Cannot transition order from " + from + " to " + to);
    }
}
