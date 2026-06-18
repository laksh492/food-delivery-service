package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RestaurantSpecifications {

    private RestaurantSpecifications() {
    }

    public static Specification<Restaurant> fromCriteria(RestaurantSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getCity() != null) {
                predicates.add(cb.equal(root.get("city"), criteria.getCity()));
            }
            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }
            if (criteria.getCuisine() != null) {
                predicates.add(cb.isMember(criteria.getCuisine(), root.get("cuisines")));
            }
            if (criteria.getMinRating() != null && criteria.getMinRating() > 0) {
                // averageRating = ratingSum / reviewCount >= minRating
                Expression<Double> averageRating = cb.quot(
                        cb.toDouble(root.get("ratingSum")),
                        cb.toDouble(root.get("reviewCount"))).as(Double.class);
                predicates.add(cb.and(
                        cb.greaterThan(root.get("reviewCount"), 0),
                        cb.greaterThanOrEqualTo(averageRating, cb.literal(criteria.getMinRating()))));
            }
            if (criteria.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), criteria.getActive()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
