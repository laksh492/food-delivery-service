package com.fooddelivery.repository.inmemory;

import com.fooddelivery.model.Rating;
import com.fooddelivery.repository.RatingRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryRatingRepository implements RatingRepository {

    private final Map<Integer, Rating> store = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> orderIdIndex = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public Rating save(Rating rating) {
        if (rating.getId() == null) {
            rating.setId(seq.incrementAndGet());
        }
        store.put(rating.getId(), rating);
        orderIdIndex.put(rating.getOrderId(), rating.getId());
        return rating;
    }

    @Override
    public Optional<Rating> findByOrderId(Integer orderId) {
        Integer ratingId = orderIdIndex.get(orderId);
        if (ratingId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(ratingId));
    }

    @Override
    public boolean existsByOrderId(Integer orderId) {
        return orderIdIndex.containsKey(orderId);
    }
}
