package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.MenuItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMenuItemRepository implements MenuItemRepository {

    private final MenuItemSpringDataRepository delegate;

    @Override
    @Transactional
    public MenuItem save(MenuItem menuItem) {
        return delegate.save(menuItem);
    }

    @Override
    public Optional<MenuItem> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public List<MenuItem> findByRestaurantId(Integer restaurantId) {
        return delegate.findByRestaurantId(restaurantId);
    }

    @Override
    @Transactional
    public boolean reserveStock(Integer itemId, int quantity) {
        return delegate.decrementStock(itemId, quantity) == 1;
    }

    @Override
    @Transactional
    public void releaseStock(Integer itemId, int quantity) {
        delegate.incrementStock(itemId, quantity);
    }
}
