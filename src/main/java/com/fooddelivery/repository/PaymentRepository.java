package com.fooddelivery.repository;

import com.fooddelivery.model.Payment;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Integer id);

    Optional<Payment> findByOrderId(Integer orderId);
}
