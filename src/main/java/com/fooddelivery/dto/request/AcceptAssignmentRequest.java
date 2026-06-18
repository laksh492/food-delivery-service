package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

@Value
public class AcceptAssignmentRequest {

    @NotNull
    @Positive
    Integer partnerId;
}
