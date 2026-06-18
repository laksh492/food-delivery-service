package com.fooddelivery.dto.request;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CreateUserRequest {

    @NotBlank
    String name;

    @NotBlank
    String phone;

    @NotNull
    Role role;

    City city;
}
