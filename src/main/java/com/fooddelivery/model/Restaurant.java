package com.fooddelivery.model;

import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private City city;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "restaurant_cuisines", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine", length = 20)
    private Set<Cuisine> cuisines = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "rating_sum", nullable = false)
    private long ratingSum;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Version
    @Column(nullable = false)
    private int version;

    public Restaurant(CreateRestaurantRequest request) {
        this.city = request.getCity();
        this.ownerId = request.getOwnerId();
        this.name = request.getName();
        this.cuisines = new HashSet<>(request.getCuisines());
        this.active = true;
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
