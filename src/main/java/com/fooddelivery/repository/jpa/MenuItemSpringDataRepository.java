package com.fooddelivery.repository.jpa;

import com.fooddelivery.model.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemSpringDataRepository
        extends JpaRepository<MenuItem, Integer>, JpaSpecificationExecutor<MenuItem> {

    List<MenuItem> findByRestaurantId(Integer restaurantId);

    @Modifying
    @Query("""
            UPDATE MenuItem m
            SET m.availableStock = m.availableStock - :quantity,
                m.version = m.version + 1
            WHERE m.id = :id
              AND m.available = true
              AND m.availableStock >= :quantity
            """)
    int decrementStock(@Param("id") Integer id, @Param("quantity") int quantity);

    @Modifying
    @Query("""
            UPDATE MenuItem m
            SET m.availableStock = m.availableStock + :quantity,
                m.version = m.version + 1
            WHERE m.id = :id
            """)
    void incrementStock(@Param("id") Integer id, @Param("quantity") int quantity);
}
