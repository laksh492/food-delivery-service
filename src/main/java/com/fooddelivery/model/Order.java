package com.fooddelivery.model;

import com.fooddelivery.enums.City;
import com.fooddelivery.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private City city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Transient
    private Integer paymentId;

    @Column(name = "assigned_partner_id")
    private Integer assignedPartnerId;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Version
    @Column(nullable = false)
    private int version;

    public Order(Integer customerId, Restaurant restaurant, List<OrderItem> items, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.restaurantId = restaurant.getId();
        this.city = restaurant.getCity();
        this.status = OrderStatus.PENDING_PAYMENT;
        this.items = new ArrayList<>(items);
        for (OrderItem item : this.items) {
            item.setOrder(this);
        }
        this.totalAmount = totalAmount;
        this.placedAt = LocalDateTime.now();
    }

    public void assignPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public void markDelivered() {
        this.deliveredAt = LocalDateTime.now();
    }
}
