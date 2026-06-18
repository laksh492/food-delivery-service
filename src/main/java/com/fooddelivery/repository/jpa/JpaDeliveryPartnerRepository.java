package com.fooddelivery.repository.jpa;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PartnerStatus;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.repository.DeliveryPartnerRepository;
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
public class JpaDeliveryPartnerRepository implements DeliveryPartnerRepository {

    private final DeliveryPartnerSpringDataRepository delegate;

    @Override
    @Transactional
    public DeliveryPartner save(DeliveryPartner partner) {
        return delegate.save(partner);
    }

    @Override
    public Optional<DeliveryPartner> findById(Integer id) {
        return delegate.findById(id);
    }

    @Override
    public List<DeliveryPartner> findAvailableByCity(City city) {
        return delegate.findByCityAndStatus(city, PartnerStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public boolean markBusyIfAvailable(Integer partnerId, Integer orderId, int expectedVersion) {
        return delegate.markBusyIfAvailable(partnerId, orderId, expectedVersion) == 1;
    }

    @Override
    @Transactional
    public void markAvailable(Integer partnerId) {
        delegate.markAvailable(partnerId);
    }
}
