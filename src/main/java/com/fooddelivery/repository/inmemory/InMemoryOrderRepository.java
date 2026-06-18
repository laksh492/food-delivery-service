package com.fooddelivery.repository.inmemory;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import com.fooddelivery.repository.OrderRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Integer, Order> store = new ConcurrentHashMap<>();
    private final Map<Integer, Object> locks = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);
    private final AtomicInteger itemSeq = new AtomicInteger(0);

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(seq.incrementAndGet());
            assignOrderItemIds(order);
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findByRestaurantId(Integer restaurantId) {
        return store.values().stream()
                .filter(order -> order.getRestaurantId().equals(restaurantId))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();
    }

    @Override
    public Page<Order> findUnassignedByCity(City city, Pageable pageable) {
        List<Order> filtered = store.values().stream()
                .filter(order -> order.getCity() == city)
                .filter(order -> order.getAssignedPartnerId() == null)
                .filter(order -> order.getStatus() == OrderStatus.ACCEPTED)
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Order> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    @Override
    public boolean assignPartnerIfUnassigned(Integer orderId, Integer partnerId, int expectedVersion) {
        synchronized (lockFor(orderId)) {
            Order order = store.get(orderId);
            if (order == null || order.getAssignedPartnerId() != null) {
                return false;
            }
            order.setAssignedPartnerId(partnerId);
            return true;
        }
    }

    private Object lockFor(Integer orderId) {
        return locks.computeIfAbsent(orderId, id -> new Object());
    }

    private void assignOrderItemIds(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getId() == null) {
                item.setId(itemSeq.incrementAndGet());
            }
            item.setOrderId(order.getId());
        }
    }
}
