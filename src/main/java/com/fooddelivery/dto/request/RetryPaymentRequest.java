package com.fooddelivery.dto.request;

import com.fooddelivery.enums.PaymentScenario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetryPaymentRequest {

    private PaymentScenario paymentScenario;
}
