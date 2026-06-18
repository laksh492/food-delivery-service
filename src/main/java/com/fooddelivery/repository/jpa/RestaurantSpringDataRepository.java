package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestaurantSpringDataRepository
        extends JpaRepository<Restaurant, Integer>, JpaSpecificationExecutor<Restaurant> {
}
