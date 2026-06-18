package com.fooddelivery.repository;

import com.fooddelivery.model.Restaurant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepository {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(Integer id);

    List<Restaurant> findAll();

    Page<Restaurant> search(RestaurantSearchCriteria criteria, Pageable pageable);
}
