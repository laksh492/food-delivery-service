package com.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.CreateRatingRequest;
import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.inmemory.InMemoryDeliveryPartnerRepository;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.repository.inmemory.InMemoryOrderRepository;
import com.fooddelivery.repository.inmemory.InMemoryRatingRepository;
import com.fooddelivery.repository.inmemory.InMemoryRestaurantRepository;
import com.fooddelivery.repository.inmemory.InMemoryUserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RatingServiceTest {

    private InMemoryRatingRepository ratingRepository;
    private InMemoryOrderRepository orderRepository;
    private InMemoryRestaurantRepository restaurantRepository;
    private InMemoryDeliveryPartnerRepository deliveryPartnerRepository;
    private RatingService ratingService;

    private User customer;
    private Restaurant restaurant;
    private DeliveryPartner partner;
    private Order deliveredOrder;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryMenuItemRepository menuItemRepository = new InMemoryMenuItemRepository();
        ratingRepository = new InMemoryRatingRepository();
        orderRepository = new InMemoryOrderRepository();
        restaurantRepository = new InMemoryRestaurantRepository();
        deliveryPartnerRepository = new InMemoryDeliveryPartnerRepository();
        ratingService = new RatingService(
                ratingRepository, orderRepository, restaurantRepository, deliveryPartnerRepository);

        customer = userRepository.save(new User(
                new CreateUserRequest("Customer", "9000000001", Role.CUSTOMER, City.BANGALORE)));

        restaurant = new Restaurant();
        restaurant.setCity(City.BANGALORE);
        restaurant.setOwnerId(customer.getId());
        restaurant.setName("Spice Hub");
        restaurant.setCuisines(Set.of(Cuisine.INDIAN));
        restaurant = restaurantRepository.save(restaurant);

        MenuItem menuItem = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Biryani", "Spicy", new BigDecimal("200.00"), 5)));

        deliveredOrder = new Order(customer.getId(), restaurant,
                List.of(new com.fooddelivery.model.OrderItem(menuItem, 1)), new BigDecimal("200.00"));
        deliveredOrder.setStatus(OrderStatus.DELIVERED);
        partner = deliveryPartnerRepository.save(new DeliveryPartner(customer.getId(), City.BANGALORE));
        deliveredOrder.setAssignedPartnerId(partner.getId());
        deliveredOrder = orderRepository.save(deliveredOrder);
    }

    @Test
    void createRating_deliveredOrder_updatesRestaurantAndPartner() {
        CreateRatingRequest request = new CreateRatingRequest(5, 4, "Great food");

        var result = ratingService.createRating(deliveredOrder.getId(), customer.getId(), request);

        assertThat(result.getRestaurantStars()).isEqualTo(5);
        assertThat(restaurantRepository.findById(restaurant.getId()).orElseThrow().getReviewCount()).isEqualTo(1);
        assertThat(deliveryPartnerRepository.findById(partner.getId()).orElseThrow().getRatingSum()).isEqualTo(4);
    }

    @Test
    void createRating_notDelivered_throwsRatingNotAllowed() {
        deliveredOrder.setStatus(OrderStatus.PLACED);
        orderRepository.save(deliveredOrder);

        assertThatThrownBy(() -> ratingService.createRating(
                deliveredOrder.getId(), customer.getId(), new CreateRatingRequest(5, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RATING_NOT_ALLOWED);
    }

    @Test
    void createRating_alreadyRated_throwsRatingNotAllowed() {
        ratingService.createRating(deliveredOrder.getId(), customer.getId(), new CreateRatingRequest(5, 4, null));

        assertThatThrownBy(() -> ratingService.createRating(
                deliveredOrder.getId(), customer.getId(), new CreateRatingRequest(4, 3, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RATING_NOT_ALLOWED);
    }

    @Test
    void createRating_wrongCustomer_throwsAccessDenied() {
        assertThatThrownBy(() -> ratingService.createRating(
                deliveredOrder.getId(), 999, new CreateRatingRequest(5, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void createRating_partnerAssignedButNoPartnerStars_throwsValidationError() {
        assertThatThrownBy(() -> ratingService.createRating(
                deliveredOrder.getId(), customer.getId(), new CreateRatingRequest(5, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void createRating_orderNotFound_throwsNotFound() {
        assertThatThrownBy(() -> ratingService.createRating(
                999, customer.getId(), new CreateRatingRequest(5, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
