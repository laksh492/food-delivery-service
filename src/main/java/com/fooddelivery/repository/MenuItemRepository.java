package com.fooddelivery.repository;

import com.fooddelivery.model.MenuItem;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {

    MenuItem save(MenuItem menuItem);

    Optional<MenuItem> findById(Integer id);

    List<MenuItem> findByRestaurantId(Integer restaurantId);

    boolean reserveStock(Integer itemId, int quantity);

    void releaseStock(Integer itemId, int quantity);
}
