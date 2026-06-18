package com.fooddelivery.model;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private boolean available = true;

    @Version
    @Column(nullable = false)
    private int version;

    public MenuItem(Integer restaurantId, CreateMenuItemRequest request) {
        this.restaurantId = restaurantId;
        this.name = request.getName();
        this.description = request.getDescription();
        this.price = request.getPrice();
        this.availableStock = request.getAvailableStock();
        this.available = true;
    }

    public void updateFrom(UpdateMenuItemRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.price = request.getPrice();
        this.available = request.getAvailable();
    }

    public void updateStock(int availableStock) {
        this.availableStock = availableStock;
    }
}
