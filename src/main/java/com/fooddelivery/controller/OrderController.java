package com.fooddelivery.controller;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.config.RequiresRole;
import com.fooddelivery.dto.request.CreateRatingRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.dto.request.RetryPaymentRequest;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Rating;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final RatingService ratingService;

    @PostMapping
    @RequiresRole(Role.CUSTOMER)
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                            HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(userId, request));
    }

    @GetMapping("/{orderId}")
    @RequiresRole(Role.CUSTOMER)
    public ResponseEntity<Order> getOrder(@PathVariable Integer orderId, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.getOrderForCustomer(orderId, userId));
    }

    @PostMapping("/{orderId}/cancel")
    @RequiresRole(Role.CUSTOMER)
    public ResponseEntity<Order> cancelOrder(@PathVariable Integer orderId, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId));
    }

    @PostMapping("/{orderId}/retry-payment")
    @RequiresRole(Role.CUSTOMER)
    public ResponseEntity<Order> retryPayment(@PathVariable Integer orderId,
                                              @Valid @RequestBody(required = false) RetryPaymentRequest request,
                                              HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        RetryPaymentRequest effectiveRequest = request != null ? request : new RetryPaymentRequest(null);
        return ResponseEntity.ok(orderService.retryPayment(orderId, userId, effectiveRequest));
    }

    @PostMapping("/{orderId}/ratings")
    @RequiresRole(Role.CUSTOMER)
    public ResponseEntity<Rating> createRating(@PathVariable Integer orderId,
                                               @Valid @RequestBody CreateRatingRequest request,
                                               HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.createRating(orderId, userId, request));
    }
}
