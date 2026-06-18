package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Value;

@Value
public class UpdateMenuItemRequest {

    @NotBlank
    String name;

    String description;

    @NotNull
    @Positive
    BigDecimal price;

    @NotNull
    Boolean available;
}
