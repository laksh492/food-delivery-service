package com.fooddelivery.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.request.RetryPaymentRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.repository.inmemory.InMemoryOrderRepository;
import com.fooddelivery.repository.inmemory.InMemoryPaymentRepository;
import com.fooddelivery.repository.inmemory.InMemoryRestaurantRepository;
import com.fooddelivery.repository.inmemory.InMemoryUserRepository;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.PaymentService;
import com.fooddelivery.service.RestaurantService;
import com.fooddelivery.gateway.SimulatedPaymentGateway;
import com.fooddelivery.support.RecordingNotificationPublisher;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryPaymentConcurrencyTest {

    private OrderService orderService;
    private Order paymentFailedOrder;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryRestaurantRepository restaurantRepository = new InMemoryRestaurantRepository();
        InMemoryMenuItemRepository menuItemRepository = new InMemoryMenuItemRepository();
        InMemoryOrderRepository orderRepository = new InMemoryOrderRepository();
        InMemoryPaymentRepository paymentRepository = new InMemoryPaymentRepository();

        RestaurantService restaurantService = new RestaurantService(
                restaurantRepository, menuItemRepository, userRepository);
        PaymentService paymentService = new PaymentService(
                paymentRepository, new SimulatedPaymentGateway());
        orderService = new OrderService(
                orderRepository, restaurantService, paymentService, null, new RecordingNotificationPublisher());

        User customer = userRepository.save(new User(
                new com.fooddelivery.dto.request.CreateUserRequest(
                        "Customer", "9000000001", Role.CUSTOMER, City.BANGALORE)));

        Restaurant restaurant = new Restaurant();
        restaurant.setCity(City.BANGALORE);
        restaurant.setOwnerId(customer.getId());
        restaurant.setName("Test Kitchen");
        restaurant.setCuisines(Set.of(Cuisine.INDIAN));
        restaurant.setActive(true);
        restaurant = restaurantRepository.save(restaurant);

        MenuItem menuItem = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Biryani", "Spicy", new BigDecimal("200.00"), 1)));

        PlaceOrderRequest placeRequest = new PlaceOrderRequest();
        placeRequest.setRestaurantId(restaurant.getId());
        placeRequest.setItems(List.of(new OrderItemRequest(menuItem.getId(), 1)));
        placeRequest.setPaymentScenario(PaymentScenario.FAIL);
        paymentFailedOrder = orderService.placeOrder(customer.getId(), placeRequest);
    }

    @Test
    void concurrentRetries_onlyOneCompletesSuccessfully() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        List<Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    Order result = orderService.retryPayment(
                            paymentFailedOrder.getId(), paymentFailedOrder.getCustomerId(),
                            new RetryPaymentRequest(PaymentScenario.SUCCEED));
                    if (result.getStatus() == OrderStatus.PLACED) {
                        successes.incrementAndGet();
                    }
                } catch (AppException ex) {
                    if (ex.getErrorCode() == ErrorCode.ORDER_NOT_RETRYABLE
                            || ex.getErrorCode() == ErrorCode.INSUFFICIENT_STOCK) {
                        failures.incrementAndGet();
                    } else {
                        throw ex;
                    }
                }
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(failures.get()).isEqualTo(threadCount - 1);
    }
}
