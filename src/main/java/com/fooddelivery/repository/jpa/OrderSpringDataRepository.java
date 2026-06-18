package com.fooddelivery.repository.jpa;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.model.Order;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderSpringDataRepository extends JpaRepository<Order, Integer> {

    List<Order> findByRestaurantIdOrderByPlacedAtDesc(Integer restaurantId);

    Page<Order> findByCityAndAssignedPartnerIdIsNullAndStatus(
            City city, OrderStatus status, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Order o
            SET o.assignedPartnerId = :partnerId,
                o.version = o.version + 1
            WHERE o.id = :orderId
              AND o.version = :expectedVersion
              AND o.assignedPartnerId IS NULL
            """)
    int assignPartnerIfUnassigned(@Param("orderId") Integer orderId,
                                  @Param("partnerId") Integer partnerId,
                                  @Param("expectedVersion") int expectedVersion);
}
