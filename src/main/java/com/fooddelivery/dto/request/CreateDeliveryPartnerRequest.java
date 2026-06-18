package com.fooddelivery.dto.request;

import com.fooddelivery.enums.City;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

@Value
public class CreateDeliveryPartnerRequest {

    @NotNull
    @Positive
    Integer userId;

    @NotNull
    City city;
}
