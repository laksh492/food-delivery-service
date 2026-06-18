package com.fooddelivery.dto.response;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RestaurantResponse {

    Integer id;
    City city;
    Integer ownerId;
    String name;
    Set<Cuisine> cuisines;
    boolean active;
    double averageRating;
    int reviewCount;
}
