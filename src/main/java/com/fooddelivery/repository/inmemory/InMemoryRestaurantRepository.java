package com.fooddelivery.repository.inmemory;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final Map<Integer, Restaurant> store = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public Restaurant save(Restaurant restaurant) {
        if (restaurant.getId() == null) {
            restaurant.setId(seq.incrementAndGet());
        }
        store.put(restaurant.getId(), restaurant);
        return restaurant;
    }

    @Override
    public Optional<Restaurant> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Page<Restaurant> search(RestaurantSearchCriteria criteria, Pageable pageable) {
        List<Restaurant> filtered = store.values().stream()
                .filter(matches(criteria))
                .sorted(buildComparator(pageable.getSort()))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Restaurant> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private Predicate<Restaurant> matches(RestaurantSearchCriteria criteria) {
        return restaurant -> {
            if (criteria.getCity() != null && restaurant.getCity() != criteria.getCity()) {
                return false;
            }
            if (criteria.getName() != null && !restaurant.getName().toLowerCase()
                    .contains(criteria.getName().toLowerCase())) {
                return false;
            }
            if (criteria.getCuisine() != null && !restaurant.getCuisines().contains(criteria.getCuisine())) {
                return false;
            }
            if (criteria.getMinRating() != null
                    && restaurant.getAverageRating() < criteria.getMinRating()) {
                return false;
            }
            if (criteria.getActive() != null && restaurant.isActive() != criteria.getActive()) {
                return false;
            }
            return true;
        };
    }

    private Comparator<Restaurant> buildComparator(Sort sort) {
        Comparator<Restaurant> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Restaurant> next = comparatorFor(order);
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        if (comparator == null) {
            comparator = Comparator.comparing(Restaurant::getId);
        }
        return comparator;
    }

    private Comparator<Restaurant> comparatorFor(Sort.Order order) {
        Comparator<Restaurant> base = switch (order.getProperty()) {
            case "name" -> Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER);
            case "ratingSum", "rating" -> Comparator.comparingDouble(Restaurant::getAverageRating);
            default -> Comparator.comparing(Restaurant::getId);
        };
        return order.isAscending() ? base : base.reversed();
    }
}
