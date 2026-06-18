package com.fooddelivery.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FieldError {

    String field;
    String message;
}
