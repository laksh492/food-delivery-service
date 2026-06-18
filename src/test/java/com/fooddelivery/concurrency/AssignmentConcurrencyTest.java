package com.fooddelivery.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.inmemory.InMemoryDeliveryPartnerRepository;
import com.fooddelivery.repository.inmemory.InMemoryOrderRepository;
import com.fooddelivery.service.DeliveryPartnerService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignmentConcurrencyTest {

    private InMemoryOrderRepository orderRepository;
    private InMemoryDeliveryPartnerRepository partnerRepository;
    private DeliveryPartnerService deliveryPartnerService;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
        partnerRepository = new InMemoryDeliveryPartnerRepository();
        deliveryPartnerService = new DeliveryPartnerService(partnerRepository, orderRepository, null);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1);
        restaurant.setCity(City.BANGALORE);

        Order order = new Order();
        order.setId(100);
        order.setRestaurantId(1);
        order.setCity(City.BANGALORE);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setItems(List.of());
        orderRepository.save(order);

        for (int i = 1; i <= 10; i++) {
            DeliveryPartner partner = new DeliveryPartner(100 + i, City.BANGALORE);
            partner.setId(i);
            partnerRepository.save(partner);
        }
    }

    @Test
    void onlyOnePartnerWinsAssignmentRace() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int partnerId = 1; partnerId <= threadCount; partnerId++) {
            int userId = 100 + partnerId;
            int pid = partnerId;
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    deliveryPartnerService.acceptAssignment(100, pid, userId);
                    return "SUCCESS";
                } catch (AppException ex) {
                    return ex.getErrorCode().name();
                }
            }));
        }

        start.countDown();
        executor.shutdown();

        long successes = 0;
        long alreadyTaken = 0;
        for (Future<String> future : futures) {
            String result = future.get();
            if ("SUCCESS".equals(result)) {
                successes++;
            } else if (ErrorCode.ASSIGNMENT_ALREADY_TAKEN.name().equals(result)) {
                alreadyTaken++;
            }
        }

        assertThat(successes).isEqualTo(1);
        assertThat(alreadyTaken).isEqualTo(threadCount - 1);

        Order order = orderRepository.findById(100).orElseThrow();
        assertThat(order.getAssignedPartnerId()).isNotNull();
    }
}
