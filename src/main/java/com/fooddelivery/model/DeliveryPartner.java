package com.fooddelivery.model;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PartnerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status = PartnerStatus.AVAILABLE;

    @Column(name = "current_order_id")
    private Integer currentOrderId;

    @Column(name = "rating_sum", nullable = false)
    private long ratingSum;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Version
    @Column(nullable = false)
    private int version;

    public DeliveryPartner(Integer userId, City city) {
        this.userId = userId;
        this.city = city;
        this.status = PartnerStatus.AVAILABLE;
    }

    public void addRating(int stars) {
        this.ratingSum += stars;
        this.reviewCount++;
    }

    public double getAverageRating() {
        if (reviewCount == 0) {
            return 0.0;
        }
        return (double) ratingSum / reviewCount;
    }
}
