package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Rating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingSpringDataRepository extends JpaRepository<Rating, Integer> {

    Optional<Rating> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);
}
