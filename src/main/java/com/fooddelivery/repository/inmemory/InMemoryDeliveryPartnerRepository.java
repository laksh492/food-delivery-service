package com.fooddelivery.repository.inmemory;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PartnerStatus;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryDeliveryPartnerRepository implements DeliveryPartnerRepository {

    private final Map<Integer, DeliveryPartner> store = new ConcurrentHashMap<>();
    private final Map<Integer, Object> locks = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public DeliveryPartner save(DeliveryPartner partner) {
        if (partner.getId() == null) {
            partner.setId(seq.incrementAndGet());
        }
        store.put(partner.getId(), partner);
        return partner;
    }

    @Override
    public Optional<DeliveryPartner> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DeliveryPartner> findAvailableByCity(City city) {
        return store.values().stream()
                .filter(partner -> partner.getCity() == city)
                .filter(partner -> partner.getStatus() == PartnerStatus.AVAILABLE)
                .toList();
    }

    @Override
    public boolean markBusyIfAvailable(Integer partnerId, Integer orderId, int expectedVersion) {
        synchronized (lockFor(partnerId)) {
            DeliveryPartner partner = store.get(partnerId);
            if (partner == null || partner.getStatus() != PartnerStatus.AVAILABLE) {
                return false;
            }
            partner.setStatus(PartnerStatus.BUSY);
            partner.setCurrentOrderId(orderId);
            return true;
        }
    }

    @Override
    public void markAvailable(Integer partnerId) {
        synchronized (lockFor(partnerId)) {
            DeliveryPartner partner = store.get(partnerId);
            if (partner == null) {
                return;
            }
            partner.setStatus(PartnerStatus.AVAILABLE);
            partner.setCurrentOrderId(null);
        }
    }

    private Object lockFor(Integer partnerId) {
        return locks.computeIfAbsent(partnerId, id -> new Object());
    }
}
