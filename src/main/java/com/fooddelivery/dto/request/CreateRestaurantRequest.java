package com.fooddelivery.dto.request;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.Value;

@Value
public class CreateRestaurantRequest {

    @NotNull
    City city;

    @NotNull
    @Positive
    Integer ownerId;

    @NotBlank
    String name;

    @NotEmpty
    Set<Cuisine> cuisines;
}
