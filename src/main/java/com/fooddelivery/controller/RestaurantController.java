package com.fooddelivery.controller;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.config.RequiresRole;
import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.RejectOrderRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateStockRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.RestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    @GetMapping("/cities/{city}/restaurants")
    public ResponseEntity<Page<Restaurant>> searchRestaurants(
            @PathVariable City city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Cuisine cuisine,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        RestaurantSearchCriteria criteria = RestaurantSearchCriteria.builder()
                .city(city)
                .name(name)
                .cuisine(cuisine)
                .minRating(minRating)
                .active(active)
                .build();
        return ResponseEntity.ok(restaurantService.searchRestaurants(criteria, page, size, sort));
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getRestaurant(id));
    }

    @GetMapping("/restaurants/{id}/menu-items")
    public ResponseEntity<List<MenuItem>> getMenuItems(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getMenuItems(id));
    }

    @PostMapping("/restaurants/{id}/menu-items")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<MenuItem> createMenuItem(@PathVariable Integer id,
                                                   @Valid @RequestBody CreateMenuItemRequest request,
                                                   HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createMenuItem(id, request, userId));
    }

    @PutMapping("/restaurants/{id}/menu-items/{itemId}")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Integer id,
                                                   @PathVariable Integer itemId,
                                                   @Valid @RequestBody UpdateMenuItemRequest request,
                                                   HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(restaurantService.updateMenuItem(id, itemId, request, userId));
    }

    @PatchMapping("/restaurants/{id}/menu-items/{itemId}/stock")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<MenuItem> updateStock(@PathVariable Integer id,
                                                  @PathVariable Integer itemId,
                                                  @Valid @RequestBody UpdateStockRequest request,
                                                  HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(restaurantService.updateStock(id, itemId, request, userId));
    }

    @GetMapping("/restaurants/{id}/orders")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<List<Order>> getRestaurantOrders(@PathVariable Integer id,
                                                           HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(id, userId));
    }

    @PostMapping("/restaurants/{id}/orders/{orderId}/accept")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<Order> acceptOrder(@PathVariable Integer id,
                                             @PathVariable Integer orderId,
                                             HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.acceptOrder(id, orderId, userId));
    }

    @PostMapping("/restaurants/{id}/orders/{orderId}/reject")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<Order> rejectOrder(@PathVariable Integer id,
                                             @PathVariable Integer orderId,
                                             @Valid @RequestBody(required = false) RejectOrderRequest request,
                                             HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.rejectOrder(id, orderId, userId));
    }

    @PostMapping("/restaurants/{id}/orders/{orderId}/start-preparing")
    @RequiresRole(Role.RESTAURANT_OWNER)
    public ResponseEntity<Order> startPreparing(@PathVariable Integer id,
                                                @PathVariable Integer orderId,
                                                HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.startPreparing(id, orderId, userId));
    }
}
