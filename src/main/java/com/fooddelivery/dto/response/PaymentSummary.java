package com.fooddelivery.dto.response;

import com.fooddelivery.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentSummary {

    Integer id;
    Integer orderId;
    BigDecimal amount;
    PaymentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
