package com.fooddelivery.dto.response;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PartnerStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeliveryPartnerResponse {

    Integer id;
    Integer userId;
    City city;
    PartnerStatus status;
    Integer currentOrderId;
    double averageRating;
    int reviewCount;
}
