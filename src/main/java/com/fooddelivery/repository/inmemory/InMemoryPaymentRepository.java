package com.fooddelivery.repository.inmemory;

import com.fooddelivery.model.Payment;
import com.fooddelivery.repository.PaymentRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Integer, Payment> store = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> orderIdIndex = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(seq.incrementAndGet());
        }
        store.put(payment.getId(), payment);
        orderIdIndex.put(payment.getOrderId(), payment.getId());
        return payment;
    }

    @Override
    public Optional<Payment> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Payment> findByOrderId(Integer orderId) {
        Integer paymentId = orderIdIndex.get(orderId);
        if (paymentId == null) {
            return Optional.empty();
        }
        return findById(paymentId);
    }
}
