package com.fooddelivery.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MenuItemResponse {

    Integer id;
    Integer restaurantId;
    String name;
    String description;
    BigDecimal price;
    int availableStock;
    boolean available;
}
