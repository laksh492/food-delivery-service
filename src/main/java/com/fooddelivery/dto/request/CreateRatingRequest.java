package com.fooddelivery.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CreateRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    Integer restaurantStars;

    @Min(1)
    @Max(5)
    Integer partnerStars;

    String review;
}
