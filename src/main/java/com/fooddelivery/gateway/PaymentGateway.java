package com.fooddelivery.gateway;

import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.PaymentStatus;

public interface PaymentGateway {

    PaymentStatus charge(PaymentScenario scenarioHint);
}
