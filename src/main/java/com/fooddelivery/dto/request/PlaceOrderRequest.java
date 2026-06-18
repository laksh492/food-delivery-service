package com.fooddelivery.dto.request;

import com.fooddelivery.enums.PaymentScenario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotNull
    @Positive
    private Integer restaurantId;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    private PaymentScenario paymentScenario;

    /** Defaults to {@link PaymentScenario#SUCCEED} when omitted or null. */
    public PaymentScenario getPaymentScenario() {
        return paymentScenario != null ? paymentScenario : PaymentScenario.SUCCEED;
    }
}
