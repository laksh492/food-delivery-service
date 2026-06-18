package com.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.CreateUserRequest;
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
import com.fooddelivery.gateway.SimulatedPaymentGateway;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.repository.inmemory.InMemoryOrderRepository;
import com.fooddelivery.repository.inmemory.InMemoryPaymentRepository;
import com.fooddelivery.repository.inmemory.InMemoryRestaurantRepository;
import com.fooddelivery.repository.inmemory.InMemoryUserRepository;
import com.fooddelivery.support.RecordingNotificationPublisher;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private InMemoryUserRepository userRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    private InMemoryMenuItemRepository menuItemRepository;
    private InMemoryOrderRepository orderRepository;
    private InMemoryPaymentRepository paymentRepository;
    private RestaurantService restaurantService;
    private PaymentService paymentService;
    private RecordingNotificationPublisher notificationPublisher;
    private OrderService orderService;

    private User customer;
    private User owner;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        menuItemRepository = new InMemoryMenuItemRepository();
        orderRepository = new InMemoryOrderRepository();
        paymentRepository = new InMemoryPaymentRepository();

        restaurantService = new RestaurantService(restaurantRepository, menuItemRepository, userRepository);
        paymentService = new PaymentService(paymentRepository, new SimulatedPaymentGateway());
        notificationPublisher = new RecordingNotificationPublisher();
        orderService = new OrderService(
                orderRepository, restaurantService, paymentService, null, notificationPublisher);

        owner = userRepository.save(new User(
                new CreateUserRequest("Owner", "9000000001", Role.RESTAURANT_OWNER, City.BANGALORE)));
        customer = userRepository.save(new User(
                new CreateUserRequest("Customer", "9000000002", Role.CUSTOMER, City.BANGALORE)));

        restaurant = new Restaurant();
        restaurant.setCity(City.BANGALORE);
        restaurant.setOwnerId(owner.getId());
        restaurant.setName("Spice Hub");
        restaurant.setCuisines(Set.of(Cuisine.INDIAN));
        restaurant.setActive(true);
        restaurant = restaurantRepository.save(restaurant);

        menuItem = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Biryani", "Spicy rice", new BigDecimal("200.00"), 5)));
    }

    @Test
    void placeOrder_successfulPayment_transitionsToPlaced() {
        Order result = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(notificationPublisher.getStatusEvents()).isNotEmpty();
        assertThat(menuItemRepository.findById(menuItem.getId()).orElseThrow().getAvailableStock()).isEqualTo(4);
    }

    @Test
    void placeOrder_failedPayment_releasesStockAndSetsPaymentFailed() {
        Order result = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.FAIL));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(menuItemRepository.findById(menuItem.getId()).orElseThrow().getAvailableStock()).isEqualTo(5);
    }

    @Test
    void placeOrder_inactiveRestaurant_throwsValidationError() {
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);

        assertThatThrownBy(() -> orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void cancelOrder_fromPlaced_releasesStockAndRefunds() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        Order result = orderService.cancelOrder(order.getId(), customer.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(menuItemRepository.findById(menuItem.getId()).orElseThrow().getAvailableStock()).isEqualTo(5);
    }

    @Test
    void cancelOrder_fromDelivered_throwsInvalidTransition() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId(), customer.getId()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ORDER_STATE_TRANSITION);
    }

    @Test
    void acceptOrder_fromPlaced_transitionsAndPublishesAssignmentOffered() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        Order result = orderService.acceptOrder(restaurant.getId(), order.getId(), owner.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(notificationPublisher.getAssignmentEvents()).hasSize(1);
    }

    @Test
    void rejectOrder_fromPlaced_releasesStockAndRefunds() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        Order result = orderService.rejectOrder(restaurant.getId(), order.getId(), owner.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(menuItemRepository.findById(menuItem.getId()).orElseThrow().getAvailableStock()).isEqualTo(5);
    }

    @Test
    void retryPayment_whenNotPaymentFailed_throwsOrderNotRetryable() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        assertThatThrownBy(() -> orderService.retryPayment(order.getId(), customer.getId(), new RetryPaymentRequest(null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_RETRYABLE);
    }

    @Test
    void retryPayment_successfulPayment_transitionsToPlaced() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.FAIL));

        Order result = orderService.retryPayment(order.getId(), customer.getId(), new RetryPaymentRequest(PaymentScenario.SUCCEED));

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void getOrderForCustomer_wrongOwner_throwsAccessDenied() {
        Order order = orderService.placeOrder(customer.getId(), buildPlaceOrderRequest(PaymentScenario.SUCCEED));

        assertThatThrownBy(() -> orderService.getOrderForCustomer(order.getId(), owner.getId()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    private PlaceOrderRequest buildPlaceOrderRequest(PaymentScenario scenario) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(restaurant.getId());
        request.setItems(List.of(new OrderItemRequest(menuItem.getId(), 1)));
        request.setPaymentScenario(scenario);
        return request;
    }
}
