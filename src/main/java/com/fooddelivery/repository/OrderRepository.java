package com.fooddelivery.repository;

import com.fooddelivery.enums.City;
import com.fooddelivery.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Integer id);

    List<Order> findByRestaurantId(Integer restaurantId);

    Page<Order> findUnassignedByCity(City city, Pageable pageable);

    boolean assignPartnerIfUnassigned(Integer orderId, Integer partnerId, int expectedVersion);
}
