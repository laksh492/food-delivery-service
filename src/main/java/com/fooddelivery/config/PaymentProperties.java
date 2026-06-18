package com.fooddelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.simulation")
public class PaymentProperties {

    private String mode = "ALWAYS_SUCCESS";
    private double successRate = 0.8;
}
