package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Payment;
import com.fooddelivery.repository.PaymentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaPaymentRepository implements PaymentRepository {

    private final PaymentSpringDataRepository delegate;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return delegate.save(payment);
    }

    @Override
    public Optional<Payment> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(Integer orderId) {
        return delegate.findByOrderId(orderId);
    }
}
