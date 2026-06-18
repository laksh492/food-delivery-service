package com.fooddelivery.dto.request;

import com.fooddelivery.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateDeliveryStatusRequest {

    @NotNull
    OrderStatus status;
}
