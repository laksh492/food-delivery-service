package com.fooddelivery.controller;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.config.RequiresRole;
import com.fooddelivery.dto.request.AcceptAssignmentRequest;
import com.fooddelivery.dto.request.UpdateDeliveryStatusRequest;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.service.DeliveryPartnerService;
import com.fooddelivery.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;
    private final OrderService orderService;

    @GetMapping("/delivery-partners/{partnerId}/assignments/available")
    @RequiresRole(Role.DELIVERY_PARTNER)
    public ResponseEntity<Page<Order>> getAvailableAssignments(
            @PathVariable Integer partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(deliveryPartnerService.getAvailableAssignments(
                partnerId, userId, PageRequest.of(page, size)));
    }

    @PostMapping("/orders/{orderId}/assignment/accept")
    @RequiresRole(Role.DELIVERY_PARTNER)
    public ResponseEntity<Order> acceptAssignment(@PathVariable Integer orderId,
                                                  @Valid @RequestBody AcceptAssignmentRequest request,
                                                  HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        return ResponseEntity.ok(deliveryPartnerService.acceptAssignment(orderId, request.getPartnerId(), userId));
    }

    @PutMapping("/orders/{orderId}/delivery-status")
    @RequiresRole(Role.DELIVERY_PARTNER)
    public ResponseEntity<Order> updateDeliveryStatus(@PathVariable Integer orderId,
                                                      @Valid @RequestBody UpdateDeliveryStatusRequest request,
                                                      HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute(AuthConstants.ATTR_USER_ID);
        Order order = orderService.getOrder(orderId);
        if (order.getAssignedPartnerId() == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "No partner assigned to this order");
        }
        DeliveryPartner partner = deliveryPartnerService.getPartner(order.getAssignedPartnerId());
        if (!partner.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Not the assigned delivery partner");
        }
        return ResponseEntity.ok(orderService.updateDeliveryStatus(orderId, partner.getId(), request));
    }
}
