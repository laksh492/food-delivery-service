package com.fooddelivery.repository;

import com.fooddelivery.model.Rating;
import java.util.Optional;

public interface RatingRepository {

    Rating save(Rating rating);

    Optional<Rating> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);
}
