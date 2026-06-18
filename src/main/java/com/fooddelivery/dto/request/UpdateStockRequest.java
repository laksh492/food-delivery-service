package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Value;

@Value
public class UpdateStockRequest {

    @NotNull
    @PositiveOrZero
    Integer availableStock;
}
