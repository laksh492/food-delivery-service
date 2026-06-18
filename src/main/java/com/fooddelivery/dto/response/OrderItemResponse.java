package com.fooddelivery.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderItemResponse {

    Integer id;
    Integer menuItemId;
    String nameSnapshot;
    BigDecimal unitPriceSnapshot;
    int quantity;
}
