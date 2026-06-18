package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Value;

@Value
public class CreateMenuItemRequest {

    @NotBlank
    String name;

    String description;

    @NotNull
    @Positive
    BigDecimal price;

    @NotNull
    @PositiveOrZero
    Integer availableStock;
}
