package com.fooddelivery.gateway;

import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentStatus charge(PaymentScenario scenarioHint) {
        if (scenarioHint == PaymentScenario.FAIL) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.SUCCESS;
    }
}
