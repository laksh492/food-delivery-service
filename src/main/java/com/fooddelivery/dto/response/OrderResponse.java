package com.fooddelivery.dto.response;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResponse {

    Integer id;
    Integer customerId;
    Integer restaurantId;
    City city;
    OrderStatus status;
    List<OrderItemResponse> items;
    BigDecimal totalAmount;
    Integer paymentId;
    Integer assignedPartnerId;
    LocalDateTime placedAt;
    LocalDateTime deliveredAt;
    PaymentSummary payment;
}
