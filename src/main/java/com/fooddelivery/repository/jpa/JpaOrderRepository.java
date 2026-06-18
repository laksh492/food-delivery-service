package com.fooddelivery.repository.jpa;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;
import com.fooddelivery.repository.OrderRepository;
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
public class JpaOrderRepository implements OrderRepository {

    private final OrderSpringDataRepository delegate;

    @Override
    @Transactional
    public Order save(Order order) {
        return delegate.save(order);
    }

    @Override
    public Optional<Order> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public List<Order> findByRestaurantId(Integer restaurantId) {
        return delegate.findByRestaurantIdOrderByPlacedAtDesc(restaurantId);
    }

    @Override
    public Page<Order> findUnassignedByCity(City city, Pageable pageable) {
        return delegate.findByCityAndAssignedPartnerIdIsNullAndStatus(
                city, OrderStatus.ACCEPTED, pageable);
    }

    @Override
    @Transactional
    public boolean assignPartnerIfUnassigned(Integer orderId, Integer partnerId, int expectedVersion) {
        return delegate.assignPartnerIfUnassigned(orderId, partnerId, expectedVersion) == 1;
    }
}
