package com.fooddelivery.dto.response;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {

    Integer id;
    String name;
    String phone;
    Role role;
    City city;
}
