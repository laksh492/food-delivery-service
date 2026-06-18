package com.fooddelivery.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RatingResponse {

    Integer id;
    Integer orderId;
    Integer customerId;
    Integer restaurantId;
    Integer partnerId;
    int restaurantStars;
    Integer partnerStars;
    String review;
    LocalDateTime createdAt;
}
