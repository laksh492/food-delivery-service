package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaRestaurantRepository implements RestaurantRepository {

    private final RestaurantSpringDataRepository delegate;

    @Override
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        return delegate.save(restaurant);
    }

    @Override
    public Optional<Restaurant> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public List<Restaurant> findAll() {
        return delegate.findAll();
    }

    @Override
    public Page<Restaurant> search(RestaurantSearchCriteria criteria, Pageable pageable) {
        return delegate.findAll(RestaurantSpecifications.fromCriteria(criteria), pageable);
    }
}
