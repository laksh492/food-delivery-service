package com.fooddelivery.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.enums.City;
import com.fooddelivery.enums.Role;
import com.fooddelivery.model.User;
import com.fooddelivery.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTestBase {

    private User customer;

    @BeforeEach
    void setUp() throws Exception {
        customer = createCustomer(City.BANGALORE);
    }

    @Test
    void protectedEndpoint_missingHeaders_returns403() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", 1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void protectedEndpoint_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", 1)
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.ADMIN.name()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void protectedEndpoint_roleHeaderMismatch_returns403() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", 1)
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.RESTAURANT_OWNER.name()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void adminEndpoint_customerRole_returns403() throws Exception {
        mockMvc.perform(get("/admin/restaurants")
                        .header(AuthConstants.HEADER_USER_ID, String.valueOf(customer.getId()))
                        .header(AuthConstants.HEADER_ROLE, Role.CUSTOMER.name()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void createUser_noAuthRequired_returns201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Open User","phone":"8888888888","role":"CUSTOMER","city":"BANGALORE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Open User"));
    }
}
