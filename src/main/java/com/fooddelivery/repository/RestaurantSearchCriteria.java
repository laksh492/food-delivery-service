package com.fooddelivery.repository;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RestaurantSearchCriteria {

    City city;
    String name;
    Cuisine cuisine;
    Double minRating;
    Boolean active;
}
