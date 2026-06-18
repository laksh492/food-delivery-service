package com.fooddelivery.repository;

import com.fooddelivery.enums.City;
import com.fooddelivery.model.DeliveryPartner;
import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository {

    DeliveryPartner save(DeliveryPartner partner);

    Optional<DeliveryPartner> findById(Integer id);

    List<DeliveryPartner> findAvailableByCity(City city);

    boolean markBusyIfAvailable(Integer partnerId, Integer orderId, int expectedVersion);

    void markAvailable(Integer partnerId);
}
