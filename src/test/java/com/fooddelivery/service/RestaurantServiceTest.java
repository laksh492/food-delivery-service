package com.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.repository.inmemory.InMemoryRestaurantRepository;
import com.fooddelivery.repository.inmemory.InMemoryUserRepository;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestaurantServiceTest {

    private InMemoryRestaurantRepository restaurantRepository;
    private InMemoryMenuItemRepository menuItemRepository;
    private InMemoryUserRepository userRepository;
    private RestaurantService restaurantService;

    private User owner;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurantRepository = new InMemoryRestaurantRepository();
        menuItemRepository = new InMemoryMenuItemRepository();
        userRepository = new InMemoryUserRepository();
        restaurantService = new RestaurantService(restaurantRepository, menuItemRepository, userRepository);

        owner = userRepository.save(new User(
                new CreateUserRequest("Owner", "9000000001", Role.RESTAURANT_OWNER, City.BANGALORE)));

        restaurant = new Restaurant();
        restaurant.setCity(City.BANGALORE);
        restaurant.setOwnerId(owner.getId());
        restaurant.setName("Spice Hub");
        restaurant.setCuisines(Set.of(Cuisine.INDIAN));
        restaurant.setActive(true);
        restaurant = restaurantRepository.save(restaurant);
    }

    @Test
    void createRestaurant_ownerNotFound_throwsNotFound() {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                City.BANGALORE, 99, "New Place", Set.of(Cuisine.INDIAN));

        assertThatThrownBy(() -> restaurantService.createRestaurant(request))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void createRestaurant_validOwner_savesRestaurant() {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                City.BANGALORE, owner.getId(), "New Place", Set.of(Cuisine.INDIAN));

        Restaurant result = restaurantService.createRestaurant(request);

        assertThat(result.getName()).isEqualTo("New Place");
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void getRestaurant_notFound_throwsNotFound() {
        assertThatThrownBy(() -> restaurantService.getRestaurant(999))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void reserveStock_insufficientStock_throwsConflict() {
        MenuItem item = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Limited", "Only one", new BigDecimal("50.00"), 0)));

        assertThatThrownBy(() -> restaurantService.reserveStock(item.getId(), 1))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    void reserveStock_available_decrementsStock() {
        MenuItem item = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Dish", "Tasty", new BigDecimal("50.00"), 3)));

        restaurantService.reserveStock(item.getId(), 2);

        assertThat(menuItemRepository.findById(item.getId()).orElseThrow().getAvailableStock()).isEqualTo(1);
    }

    @Test
    void validateRestaurantOwner_wrongOwner_throwsAccessDenied() {
        User other = userRepository.save(new User(
                new CreateUserRequest("Other", "9000000002", Role.RESTAURANT_OWNER, City.BANGALORE)));

        assertThatThrownBy(() -> restaurantService.validateRestaurantOwner(restaurant.getId(), other.getId()))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void getMenuItem_notFound_throwsNotFound() {
        assertThatThrownBy(() -> restaurantService.getMenuItem(999))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void releaseStock_restoresInventory() {
        MenuItem item = menuItemRepository.save(new MenuItem(restaurant.getId(), new CreateMenuItemRequest(
                "Dish", "Tasty", new BigDecimal("50.00"), 2)));

        restaurantService.reserveStock(item.getId(), 2);
        restaurantService.releaseStock(item.getId(), 1);

        assertThat(menuItemRepository.findById(item.getId()).orElseThrow().getAvailableStock()).isEqualTo(1);
    }
}
