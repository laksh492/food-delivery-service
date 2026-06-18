package com.fooddelivery.repository.inmemory;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.MenuItemRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryMenuItemRepository implements MenuItemRepository {

    private final Map<Integer, MenuItem> store = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> stockCounters = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public MenuItem save(MenuItem menuItem) {
        if (menuItem.getId() == null) {
            menuItem.setId(seq.incrementAndGet());
            stockCounters.put(menuItem.getId(), new AtomicInteger(menuItem.getAvailableStock()));
        } else {
            stockCounters.computeIfAbsent(menuItem.getId(), id -> new AtomicInteger(menuItem.getAvailableStock()))
                    .set(menuItem.getAvailableStock());
        }
        store.put(menuItem.getId(), menuItem);
        return menuItem;
    }

    @Override
    public Optional<MenuItem> findById(Integer id) {
        return Optional.ofNullable(store.get(id)).map(this::syncStock);
    }

    @Override
    public List<MenuItem> findByRestaurantId(Integer restaurantId) {
        return store.values().stream()
                .filter(item -> item.getRestaurantId().equals(restaurantId))
                .map(this::syncStock)
                .toList();
    }

    @Override
    public boolean reserveStock(Integer itemId, int quantity) {
        MenuItem item = store.get(itemId);
        if (item == null || !item.isAvailable() || quantity <= 0) {
            return false;
        }
        AtomicInteger stock = stockCounters.get(itemId);
        if (stock == null) {
            return false;
        }
        while (true) {
            int current = stock.get();
            if (current < quantity) {
                return false;
            }
            if (stock.compareAndSet(current, current - quantity)) {
                item.setAvailableStock(current - quantity);
                return true;
            }
        }
    }

    @Override
    public void releaseStock(Integer itemId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        MenuItem item = store.get(itemId);
        if (item == null) {
            return;
        }
        AtomicInteger stock = stockCounters.computeIfAbsent(itemId, id -> new AtomicInteger(item.getAvailableStock()));
        int updated = stock.addAndGet(quantity);
        item.setAvailableStock(updated);
    }

    private MenuItem syncStock(MenuItem item) {
        AtomicInteger stock = stockCounters.get(item.getId());
        if (stock != null) {
            item.setAvailableStock(stock.get());
        }
        return item;
    }
}
