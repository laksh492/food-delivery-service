package com.fooddelivery.gateway;

import com.fooddelivery.config.PaymentProperties;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.PaymentStatus;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulatedPaymentGateway implements PaymentGateway {

    private final PaymentProperties paymentProperties;

    @Override
    public PaymentStatus charge(PaymentScenario scenarioHint) {
        if (scenarioHint == PaymentScenario.SUCCEED) {
            return PaymentStatus.SUCCESS;
        }
        if (scenarioHint == PaymentScenario.FAIL) {
            return PaymentStatus.FAILED;
        }
        return resolveFromMode();
    }

    private PaymentStatus resolveFromMode() {
        String mode = paymentProperties.getMode();
        if ("ALWAYS_FAIL".equalsIgnoreCase(mode)) {
            return PaymentStatus.FAILED;
        }
        if ("ALWAYS_SUCCESS".equalsIgnoreCase(mode)) {
            return PaymentStatus.SUCCESS;
        }
        double roll = ThreadLocalRandom.current().nextDouble();
        return roll < paymentProperties.getSuccessRate() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }
}
