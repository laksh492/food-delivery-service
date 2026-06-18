package com.fooddelivery.service;

import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.request.UpdateMenuItemRequest;
import com.fooddelivery.dto.request.UpdateStockRequest;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.RestaurantSearchCriteria;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.util.SortParser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public Restaurant createRestaurant(CreateRestaurantRequest request) {
        userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Owner user not found"));

        return restaurantRepository.save(new Restaurant(request));
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> searchRestaurants(RestaurantSearchCriteria criteria, int page, int size, String sort) {
        return restaurantRepository.search(criteria,
                PageRequest.of(page, size, SortParser.parse(sort)));
    }

    @Transactional(readOnly = true)
    public List<Restaurant> listAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Restaurant getRestaurant(Integer id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Restaurant not found"));
    }

    @Transactional
    public MenuItem createMenuItem(Integer restaurantId, CreateMenuItemRequest request, Integer ownerUserId) {
        validateRestaurantOwner(restaurantId, ownerUserId);
        return menuItemRepository.save(new MenuItem(restaurantId, request));
    }

    @Transactional
    public MenuItem updateMenuItem(Integer restaurantId, Integer itemId, UpdateMenuItemRequest request,
                                   Integer ownerUserId) {
        validateRestaurantOwner(restaurantId, ownerUserId);
        MenuItem item = getMenuItemForRestaurant(restaurantId, itemId);
        item.updateFrom(request);
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem updateStock(Integer restaurantId, Integer itemId, UpdateStockRequest request,
                                Integer ownerUserId) {
        validateRestaurantOwner(restaurantId, ownerUserId);
        MenuItem item = getMenuItemForRestaurant(restaurantId, itemId);
        item.updateStock(request.getAvailableStock());
        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItems(Integer restaurantId) {
        getRestaurant(restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public void reserveStock(Integer itemId, int quantity) {
        if (!menuItemRepository.reserveStock(itemId, quantity)) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                    "Insufficient stock for menu item " + itemId);
        }
    }

    public void releaseStock(Integer itemId, int quantity) {
        menuItemRepository.releaseStock(itemId, quantity);
    }

    public MenuItem getMenuItem(Integer itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Menu item not found"));
    }

    public void validateRestaurantOwner(Integer restaurantId, Integer userId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        if (!restaurant.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Not the restaurant owner");
        }
    }

    private MenuItem getMenuItemForRestaurant(Integer restaurantId, Integer itemId) {
        MenuItem item = getMenuItem(itemId);
        if (!item.getRestaurantId().equals(restaurantId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Menu item not found for restaurant");
        }
        return item;
    }
}
