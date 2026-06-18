package com.fooddelivery.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.dto.request.CreateMenuItemRequest;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected User createUser(String name, Role role, City city) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, "9000000001", role, city);
        String json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, User.class);
    }

    protected User createAdmin() throws Exception {
        return createUser("Admin", Role.ADMIN, null);
    }

    protected User createCustomer(City city) throws Exception {
        return createUser("Customer", Role.CUSTOMER, city);
    }

    protected User createOwner(City city) throws Exception {
        return createUser("Owner", Role.RESTAURANT_OWNER, city);
    }

    protected Restaurant createRestaurant(User admin, User owner, String name) throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                City.BANGALORE, owner.getId(), name, Set.of(Cuisine.INDIAN));
        String json = mockMvc.perform(post("/admin/restaurants")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(admin.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.ADMIN.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, Restaurant.class);
    }

    protected MenuItem createMenuItem(User owner, Restaurant restaurant, int stock) throws Exception {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Biryani", "Spicy rice", new BigDecimal("250.00"), stock);
        String json = mockMvc.perform(post("/restaurants/{id}/menu-items", restaurant.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(owner.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.RESTAURANT_OWNER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, MenuItem.class);
    }

    protected Order placeOrder(User customer, Restaurant restaurant, MenuItem item) throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(restaurant.getId());
        request.setItems(List.of(new OrderItemRequest(item.getId(), 1)));
        request.setPaymentScenario(PaymentScenario.SUCCEED);

        String json = mockMvc.perform(post("/orders")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, Order.class);
    }

    protected DeliveryPartner createPartner(User admin, City city) throws Exception {
        CreateUserRequest request = new CreateUserRequest("Partner", "9000000002", Role.DELIVERY_PARTNER, city);
        String json = mockMvc.perform(post("/admin/delivery-partners")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(admin.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.ADMIN.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, DeliveryPartner.class);
    }
}
