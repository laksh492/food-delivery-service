package com.fooddelivery.service;

import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.request.RetryPaymentRequest;
import com.fooddelivery.dto.request.UpdateDeliveryStatusRequest;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import com.fooddelivery.model.Payment;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.notification.NotificationPublisher;
import com.fooddelivery.notification.event.AssignmentOfferedEvent;
import com.fooddelivery.notification.event.OrderStatusChangedEvent;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.state.OrderState;
import com.fooddelivery.state.OrderStates;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantService restaurantService;
    private final PaymentService paymentService;
    private final DeliveryPartnerService deliveryPartnerService;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public Order placeOrder(Integer customerId, PlaceOrderRequest request) {
        Restaurant restaurant = restaurantService.getRestaurant(request.getRestaurantId());
        if (!restaurant.isActive()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Restaurant is not active");
        }

        List<OrderItem> orderItems = buildOrderItems(request, restaurant.getId());
        BigDecimal totalAmount = calculateTotal(orderItems);
        List<ReservedStock> reserved = reserveStock(orderItems);

        Order order = orderRepository.save(new Order(customerId, restaurant, orderItems, totalAmount));

        try {
            Payment payment = paymentService.charge(order.getId(), totalAmount, request.getPaymentScenario());
            order.assignPaymentId(payment.getId());
            return handlePaymentResult(order, payment, reserved);
        } catch (RuntimeException ex) {
            rollbackStock(reserved);
            throw ex;
        }
    }

    @Transactional
    public Order cancelOrder(Integer orderId, Integer customerId) {
        Order order = getOrderForCustomer(orderId, customerId);
        OrderStatus previous = order.getStatus();
        if (previous != OrderStatus.PLACED && previous != OrderStatus.ACCEPTED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                    "Order cannot be cancelled in status " + previous);
        }

        OrderState state = OrderStates.of(order.getStatus());
        state.cancel(order);
        releaseOrderStock(order);
        refundIfPaid(order);
        order = orderRepository.save(order);
        publishStatusChange(order, previous);
        return order;
    }

    @Transactional
    public Order retryPayment(Integer orderId, Integer customerId, RetryPaymentRequest request) {
        Order order = getOrderForCustomer(orderId, customerId);
        if (order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new AppException(ErrorCode.ORDER_NOT_RETRYABLE, "Order is not in PAYMENT_FAILED status");
        }

        OrderStates.of(order.getStatus()).retryPayment(order);
        orderRepository.save(order);

        List<ReservedStock> reserved = reserveStock(order.getItems());
        PaymentScenario scenario = request.getPaymentScenario() != null
                ? request.getPaymentScenario()
                : PaymentScenario.SUCCEED;

        try {
            Payment payment = paymentService.charge(order.getId(), order.getTotalAmount(), scenario);
            order.assignPaymentId(payment.getId());
            return handlePaymentResult(order, payment, reserved);
        } catch (RuntimeException ex) {
            rollbackStock(reserved);
            throw ex;
        }
    }

    @Transactional
    public Order acceptOrder(Integer restaurantId, Integer orderId, Integer ownerUserId) {
        restaurantService.validateRestaurantOwner(restaurantId, ownerUserId);
        Order order = getOrderForRestaurant(restaurantId, orderId);
        OrderStatus previous = order.getStatus();
        OrderStates.of(order.getStatus()).accept(order);
        order = orderRepository.save(order);
        publishStatusChange(order, previous);
        notificationPublisher.publishAssignmentOffered(AssignmentOfferedEvent.builder()
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .city(order.getCity())
                .build());
        return order;
    }

    @Transactional
    public Order rejectOrder(Integer restaurantId, Integer orderId, Integer ownerUserId) {
        restaurantService.validateRestaurantOwner(restaurantId, ownerUserId);
        Order order = getOrderForRestaurant(restaurantId, orderId);
        OrderStatus previous = order.getStatus();
        OrderStates.of(order.getStatus()).reject(order);
        releaseOrderStock(order);
        refundIfPaid(order);
        order = orderRepository.save(order);
        publishStatusChange(order, previous);
        return order;
    }

    @Transactional
    public Order updateDeliveryStatus(Integer orderId, Integer partnerId, UpdateDeliveryStatusRequest request) {
        Order order = getOrder(orderId);
        if (order.getAssignedPartnerId() == null || !order.getAssignedPartnerId().equals(partnerId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Partner is not assigned to this order");
        }

        OrderStatus previous = order.getStatus();
        OrderStatus target = request.getStatus();
        OrderState state = OrderStates.of(order.getStatus());

        switch (target) {
            case PREPARING -> state.startPreparing(order);
            case OUT_FOR_DELIVERY -> state.markOutForDelivery(order);
            case DELIVERED -> {
                state.markDelivered(order);
                order.markDelivered();
                deliveryPartnerService.markPartnerAvailable(partnerId);
            }
            default -> throw new AppException(ErrorCode.INVALID_ORDER_STATE_TRANSITION,
                    "Unsupported delivery status update: " + target);
        }

        order = orderRepository.save(order);
        publishStatusChange(order, previous);
        return order;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public Order getOrderForCustomer(Integer orderId, Integer customerId) {
        Order order = getOrder(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Not the order owner");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurant(Integer restaurantId, Integer ownerUserId) {
        restaurantService.validateRestaurantOwner(restaurantId, ownerUserId);
        return orderRepository.findByRestaurantId(restaurantId);
    }

    private Order handlePaymentResult(Order order, Payment payment, List<ReservedStock> reserved) {
        OrderStatus previous = order.getStatus();
        OrderState state = OrderStates.of(order.getStatus());
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            state.markPaymentSuccess(order);
            order = orderRepository.save(order);
            publishStatusChange(order, previous);
            return order;
        }

        state.markPaymentFailed(order);
        rollbackStock(reserved);
        order = orderRepository.save(order);
        publishStatusChange(order, previous);
        return order;
    }

    private List<OrderItem> buildOrderItems(PlaceOrderRequest request, Integer restaurantId) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = restaurantService.getMenuItem(itemRequest.getMenuItemId());
            if (!menuItem.getRestaurantId().equals(restaurantId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Menu item " + itemRequest.getMenuItemId() + " does not belong to restaurant");
            }
            if (!menuItem.isAvailable()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                        "Menu item " + menuItem.getId() + " is unavailable");
            }
            items.add(new OrderItem(menuItem, itemRequest.getQuantity()));
        }
        return items;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ReservedStock> reserveStock(List<OrderItem> items) {
        List<ReservedStock> reserved = new ArrayList<>();
        for (OrderItem item : items) {
            try {
                restaurantService.reserveStock(item.getMenuItemId(), item.getQuantity());
                reserved.add(new ReservedStock(item.getMenuItemId(), item.getQuantity()));
            } catch (AppException ex) {
                rollbackStock(reserved);
                throw ex;
            }
        }
        return reserved;
    }

    private void rollbackStock(List<ReservedStock> reserved) {
        for (ReservedStock entry : reserved) {
            restaurantService.releaseStock(entry.menuItemId(), entry.quantity());
        }
    }

    private void releaseOrderStock(Order order) {
        for (OrderItem item : order.getItems()) {
            restaurantService.releaseStock(item.getMenuItemId(), item.getQuantity());
        }
    }

    private void refundIfPaid(Order order) {
        if (order.getPaymentId() == null) {
            Payment payment = paymentService.getByOrderId(order.getId());
            if (payment != null) {
                order.assignPaymentId(payment.getId());
            }
        }
        if (order.getPaymentId() != null) {
            paymentService.refund(order.getPaymentId());
        }
    }

    private Order getOrderForRestaurant(Integer restaurantId, Integer orderId) {
        Order order = getOrder(orderId);
        if (!order.getRestaurantId().equals(restaurantId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Order not found for restaurant");
        }
        return order;
    }

    private void publishStatusChange(Order order, OrderStatus previousStatus) {
        notificationPublisher.publishOrderStatusChanged(OrderStatusChangedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .restaurantId(order.getRestaurantId())
                .assignedPartnerId(order.getAssignedPartnerId())
                .city(order.getCity())
                .previousStatus(previousStatus)
                .newStatus(order.getStatus())
                .build());
    }

    private record ReservedStock(Integer menuItemId, Integer quantity) {
    }
}
