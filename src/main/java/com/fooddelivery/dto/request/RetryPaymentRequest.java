package com.fooddelivery.dto.request;

import com.fooddelivery.enums.PaymentScenario;
import lombok.Value;

@Value
public class RetryPaymentRequest {

    PaymentScenario paymentScenario;
}
