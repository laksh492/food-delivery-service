package com.fooddelivery.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.dto.request.AcceptAssignmentRequest;
import com.fooddelivery.dto.request.CreateRatingRequest;
import com.fooddelivery.dto.request.UpdateDeliveryStatusRequest;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.dto.request.OrderItemRequest;
import com.fooddelivery.dto.request.PlaceOrderRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.PaymentScenario;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.support.IntegrationTestBase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OrderControllerIntegrationTest extends IntegrationTestBase {

    private User admin;
    private User customer;
    private User owner;
    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() throws Exception {
        admin = createAdmin();
        customer = createCustomer(City.BANGALORE);
        owner = createOwner(City.BANGALORE);
        restaurant = createRestaurant(admin, owner, "Spice Hub");
        menuItem = createMenuItem(owner, restaurant, 10);
    }

    @Test
    void placeOrder_happyPath_returnsPlacedOrder() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(restaurant.getId());
        request.setItems(List.of(new OrderItemRequest(menuItem.getId(), 2)));
        request.setPaymentScenario(PaymentScenario.SUCCEED);

        mockMvc.perform(post("/orders")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(500.0));
    }

    @Test
    void placeOrder_validationError_returns400() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(null);
        request.setItems(List.of());

        mockMvc.perform(post("/orders")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void placeOrder_insufficientStock_returns409() throws Exception {
        MenuItem limited = createMenuItem(owner, restaurant, 0);

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(restaurant.getId());
        request.setItems(List.of(new OrderItemRequest(limited.getId(), 1)));
        request.setPaymentScenario(PaymentScenario.SUCCEED);

        mockMvc.perform(post("/orders")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", 99999)
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void cancelOrder_happyPath_returnsCancelled() throws Exception {
        Order order = placeOrder(customer, restaurant, menuItem);

        mockMvc.perform(post("/orders/{orderId}/cancel", order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void placeOrder_failedPayment_canRetryAndSucceed() throws Exception {
        PlaceOrderRequest failRequest = new PlaceOrderRequest();
        failRequest.setRestaurantId(restaurant.getId());
        failRequest.setItems(List.of(new OrderItemRequest(menuItem.getId(), 1)));
        failRequest.setPaymentScenario(PaymentScenario.FAIL);

        String failedJson = mockMvc.perform(post("/orders")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"))
                .andReturn().getResponse().getContentAsString();

        Order failedOrder = objectMapper.readValue(failedJson, Order.class);

        mockMvc.perform(post("/orders/{orderId}/retry-payment", failedOrder.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    void createRating_afterDelivery_returns201() throws Exception {
        Order order = placeAndDeliverOrder();

        CreateRatingRequest ratingRequest = new CreateRatingRequest(5, 4, "Excellent");

        mockMvc.perform(post("/orders/{orderId}/ratings", order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantStars").value(5));
    }

    @Test
    void createRating_beforeDelivery_returns409() throws Exception {
        Order order = placeOrder(customer, restaurant, menuItem);

        CreateRatingRequest ratingRequest = new CreateRatingRequest(5, null, null);

        mockMvc.perform(post("/orders/{orderId}/ratings", order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("RATING_NOT_ALLOWED"));
    }

    private Order placeAndDeliverOrder() throws Exception {
        Order order = placeOrder(customer, restaurant, menuItem);
        var partner = createPartner(admin, City.BANGALORE);

        mockMvc.perform(post("/restaurants/{id}/orders/{orderId}/accept", restaurant.getId(), order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(owner.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.RESTAURANT_OWNER.name()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/orders/{orderId}/assignment/accept", order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(partner.getUserId()))
                        .header(AuthConstants.HEADER_ROLE, Role.DELIVERY_PARTNER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AcceptAssignmentRequest(partner.getId()))))
                .andExpect(status().isOk());

        for (OrderStatus status : List.of(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED)) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .put("/orders/{orderId}/delivery-status", order.getId())
                            .header(AuthConstants.HEADER_USER_ID, String.valueOf(partner.getUserId()))
                            .header(AuthConstants.HEADER_ROLE, Role.DELIVERY_PARTNER.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateDeliveryStatusRequest(status))))
                    .andExpect(status().isOk());
        }

        String json = mockMvc.perform(get("/orders/{orderId}", order.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, Order.class);
    }
}
