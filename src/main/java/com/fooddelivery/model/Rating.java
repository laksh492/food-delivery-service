package com.fooddelivery.model;

import com.fooddelivery.dto.request.CreateRatingRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Integer orderId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;

    @Column(name = "partner_id")
    private Integer partnerId;

    @Column(name = "restaurant_stars", nullable = false)
    private int restaurantStars;

    @Column(name = "partner_stars")
    private Integer partnerStars;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Rating(Order order, Integer customerId, CreateRatingRequest request) {
        this.orderId = order.getId();
        this.customerId = customerId;
        this.restaurantId = order.getRestaurantId();
        this.partnerId = order.getAssignedPartnerId();
        this.restaurantStars = request.getRestaurantStars();
        this.partnerStars = request.getPartnerStars();
        this.review = request.getReview();
        this.createdAt = LocalDateTime.now();
    }
}
