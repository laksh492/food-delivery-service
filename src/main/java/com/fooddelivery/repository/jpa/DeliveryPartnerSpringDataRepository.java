package com.fooddelivery.repository.jpa;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PartnerStatus;
import com.fooddelivery.model.DeliveryPartner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryPartnerSpringDataRepository extends JpaRepository<DeliveryPartner, Integer> {

    List<DeliveryPartner> findByCityAndStatus(City city, PartnerStatus status);

    @Modifying
    @Query("""
            UPDATE DeliveryPartner dp
            SET dp.status = com.fooddelivery.enums.PartnerStatus.BUSY,
                dp.currentOrderId = :orderId,
                dp.version = dp.version + 1
            WHERE dp.id = :partnerId
              AND dp.version = :expectedVersion
              AND dp.status = com.fooddelivery.enums.PartnerStatus.AVAILABLE
            """)
    int markBusyIfAvailable(@Param("partnerId") Integer partnerId,
                            @Param("orderId") Integer orderId,
                            @Param("expectedVersion") int expectedVersion);

    @Modifying
    @Query("""
            UPDATE DeliveryPartner dp
            SET dp.status = com.fooddelivery.enums.PartnerStatus.AVAILABLE,
                dp.currentOrderId = NULL,
                dp.version = dp.version + 1
            WHERE dp.id = :partnerId
            """)
    void markAvailable(@Param("partnerId") Integer partnerId);
}
