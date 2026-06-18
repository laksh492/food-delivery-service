package com.fooddelivery.dto.request;

import com.fooddelivery.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {

    @NotNull
    private OrderStatus status;
}
