package com.fooddelivery.service;

import com.fooddelivery.dto.request.CreateRatingRequest;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Rating;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RatingRepository;
import com.fooddelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Transactional
    public Rating createRating(Integer orderId, Integer customerId, CreateRatingRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));
        if (!order.getCustomerId().equals(customerId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Not the order owner");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.RATING_NOT_ALLOWED, "Order must be delivered before rating");
        }
        if (ratingRepository.existsByOrderId(orderId)) {
            throw new AppException(ErrorCode.RATING_NOT_ALLOWED, "Order has already been rated");
        }
        if (order.getAssignedPartnerId() != null && request.getPartnerStars() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Partner stars required when a partner was assigned");
        }

        Rating rating = new Rating(order, customerId, request);
        rating = ratingRepository.save(rating);

        updateRestaurantRating(order.getRestaurantId(), request.getRestaurantStars());
        if (order.getAssignedPartnerId() != null && request.getPartnerStars() != null) {
            updatePartnerRating(order.getAssignedPartnerId(), request.getPartnerStars());
        }
        return rating;
    }

    private void updateRestaurantRating(Integer restaurantId, int stars) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Restaurant not found"));
        restaurant.addRating(stars);
        restaurantRepository.save(restaurant);
    }

    private void updatePartnerRating(Integer partnerId, int stars) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Delivery partner not found"));
        partner.addRating(stars);
        deliveryPartnerRepository.save(partner);
    }
}
