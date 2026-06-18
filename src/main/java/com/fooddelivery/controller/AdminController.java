package com.fooddelivery.controller;

import com.fooddelivery.config.RequiresRole;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.service.DeliveryPartnerService;
import com.fooddelivery.service.RestaurantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@RequiresRole(Role.ADMIN)
public class AdminController {

    private final RestaurantService restaurantService;
    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request));
    }

    @PostMapping("/delivery-partners")
    public ResponseEntity<DeliveryPartner> createDeliveryPartner(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryPartnerService.createPartner(request));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> listRestaurants() {
        return ResponseEntity.ok(restaurantService.listAllRestaurants());
    }
}
