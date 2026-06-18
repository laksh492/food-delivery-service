package com.fooddelivery.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {

    String errorCode;
    String message;
    Instant timestamp;
    String path;
    List<FieldError> fieldErrors;
}
