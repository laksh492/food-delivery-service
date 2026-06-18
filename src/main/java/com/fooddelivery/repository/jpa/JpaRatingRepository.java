package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Rating;
import com.fooddelivery.repository.RatingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaRatingRepository implements RatingRepository {

    private final RatingSpringDataRepository delegate;

    @Override
    @Transactional
    public Rating save(Rating rating) {
        return delegate.save(rating);
    }

    @Override
    public Optional<Rating> findByOrderId(Integer orderId) {
        return delegate.findByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderId(Integer orderId) {
        return delegate.existsByOrderId(orderId);
    }
}
