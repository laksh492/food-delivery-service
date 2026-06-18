package com.fooddelivery.notification.event;

import com.fooddelivery.enums.City;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AssignmentOfferedEvent {

    Integer orderId;
    Integer restaurantId;
    City city;
}
