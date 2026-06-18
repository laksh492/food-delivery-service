package com.fooddelivery.config;

import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.repository.RatingRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.repository.inmemory.InMemoryDeliveryPartnerRepository;
import com.fooddelivery.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddelivery.repository.inmemory.InMemoryOrderRepository;
import com.fooddelivery.repository.inmemory.InMemoryPaymentRepository;
import com.fooddelivery.repository.inmemory.InMemoryRatingRepository;
import com.fooddelivery.repository.inmemory.InMemoryRestaurantRepository;
import com.fooddelivery.repository.inmemory.InMemoryUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public UserRepository inMemoryUserRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public RestaurantRepository inMemoryRestaurantRepository() {
        return new InMemoryRestaurantRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public MenuItemRepository inMemoryMenuItemRepository() {
        return new InMemoryMenuItemRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public OrderRepository inMemoryOrderRepository() {
        return new InMemoryOrderRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public PaymentRepository inMemoryPaymentRepository() {
        return new InMemoryPaymentRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public DeliveryPartnerRepository inMemoryDeliveryPartnerRepository() {
        return new InMemoryDeliveryPartnerRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "memory", matchIfMissing = true)
    public RatingRepository inMemoryRatingRepository() {
        return new InMemoryRatingRepository();
    }
}
