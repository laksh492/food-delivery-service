package com.fooddelivery.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT),
    INVALID_ORDER_STATE_TRANSITION(HttpStatus.CONFLICT),
    ASSIGNMENT_ALREADY_TAKEN(HttpStatus.CONFLICT),
    PARTNER_UNAVAILABLE(HttpStatus.CONFLICT),
    ORDER_NOT_RETRYABLE(HttpStatus.CONFLICT),
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED),
    RATING_NOT_ALLOWED(HttpStatus.CONFLICT),
    REFUND_NOT_ALLOWED(HttpStatus.CONFLICT),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    OPTIMISTIC_LOCK_FAILURE(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
