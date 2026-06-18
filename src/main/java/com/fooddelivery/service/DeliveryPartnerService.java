package com.fooddelivery.service;

import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Transactional
    public DeliveryPartner createPartner(CreateUserRequest request) {
        if (request.getRole() != Role.DELIVERY_PARTNER) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Role must be DELIVERY_PARTNER");
        }
        if (request.getCity() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "City is required for delivery partner");
        }

        User user = userService.createUser(request);

        return deliveryPartnerRepository.save(new DeliveryPartner(user.getId(), request.getCity()));
    }

    @Transactional(readOnly = true)
    public Page<Order> getAvailableAssignments(Integer partnerId, Integer userId, Pageable pageable) {
        DeliveryPartner partner = getPartnerForUser(partnerId, userId);
        return orderRepository.findUnassignedByCity(partner.getCity(), pageable);
    }

    @Transactional
    public Order acceptAssignment(Integer orderId, Integer partnerId, Integer userId) {
        DeliveryPartner partner = getPartnerForUser(partnerId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));

        if (order.getCity() != partner.getCity()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Partner city does not match order city");
        }

        boolean assigned = orderRepository.assignPartnerIfUnassigned(orderId, partnerId, order.getVersion());
        if (!assigned) {
            throw new AppException(ErrorCode.ASSIGNMENT_ALREADY_TAKEN, "Order assignment already taken");
        }

        boolean markedBusy = deliveryPartnerRepository.markBusyIfAvailable(partnerId, orderId, partner.getVersion());
        if (!markedBusy) {
            Order latest = orderRepository.findById(orderId).orElseThrow();
            latest.setAssignedPartnerId(null);
            orderRepository.save(latest);
            throw new AppException(ErrorCode.PARTNER_UNAVAILABLE, "Delivery partner is not available");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));
    }

    public void markPartnerAvailable(Integer partnerId) {
        deliveryPartnerRepository.markAvailable(partnerId);
    }

    @Transactional(readOnly = true)
    public DeliveryPartner getPartner(Integer partnerId) {
        return deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Delivery partner not found"));
    }

    private DeliveryPartner getPartnerForUser(Integer partnerId, Integer userId) {
        DeliveryPartner partner = getPartner(partnerId);
        if (!partner.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Not the delivery partner owner");
        }
        return partner;
    }
}
