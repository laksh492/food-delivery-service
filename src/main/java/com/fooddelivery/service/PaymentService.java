package com.fooddelivery.service;

import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.gateway.PaymentGateway;
import com.fooddelivery.model.Payment;
import com.fooddelivery.repository.PaymentRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public Payment charge(Integer orderId, BigDecimal amount, PaymentScenario scenario) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> paymentRepository.save(new Payment(orderId, amount)));
        PaymentStatus result = paymentGateway.charge(scenario);
        payment.updateStatus(result);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refund(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment not found"));
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new AppException(ErrorCode.REFUND_NOT_ALLOWED, "Only successful payments can be refunded");
        }
        payment.updateStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }
}
