package com.fooddelivery.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.dto.request.CreateRestaurantRequest;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Cuisine;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.User;
import com.fooddelivery.support.IntegrationTestBase;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RestaurantControllerIntegrationTest extends IntegrationTestBase {

    private User admin;
    private User owner;

    @BeforeEach
    void setUp() throws Exception {
        admin = createAdmin();
        owner = createOwner(City.BANGALORE);
    }

    @Test
    void searchRestaurants_byCity_returnsResults() throws Exception {
        createRestaurant(admin, owner, "Search-Alpha");
        createRestaurant(admin, owner, "Search-Beta");

        mockMvc.perform(get("/cities/{city}/restaurants", City.BANGALORE)
                        .param("name", "Search-"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getRestaurant_notFound_returns404() throws Exception {
        mockMvc.perform(get("/restaurants/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void createMenuItem_asOwner_returns201() throws Exception {
        var restaurant = createRestaurant(admin, owner, "Spice Hub");

        mockMvc.perform(post("/restaurants/{id}/menu-items", restaurant.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(owner.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.RESTAURANT_OWNER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Biryani","description":"Spicy","price":250.00,"availableStock":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Biryani"));
    }

    @Test
    void createMenuItem_wrongOwner_returns403() throws Exception {
        var restaurant = createRestaurant(admin, owner, "Spice Hub");
        User otherOwner = createOwner(City.BANGALORE);

        mockMvc.perform(post("/restaurants/{id}/menu-items", restaurant.getId())
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(otherOwner.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.RESTAURANT_OWNER.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Biryani","description":"Spicy","price":250.00,"availableStock":5}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void adminCreateRestaurant_ownerNotFound_returns404() throws Exception {
        CreateRestaurantRequest request = new CreateRestaurantRequest(
                City.BANGALORE, 99999, "Ghost Kitchen", Set.of(Cuisine.INDIAN));

        mockMvc.perform(post("/admin/restaurants")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(admin.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.ADMIN.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void listRestaurants_asAdmin_returnsAll() throws Exception {
        createRestaurant(admin, owner, "Spice Hub");

        mockMvc.perform(get("/admin/restaurants")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(admin.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.ADMIN.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spice Hub"));
    }
}
