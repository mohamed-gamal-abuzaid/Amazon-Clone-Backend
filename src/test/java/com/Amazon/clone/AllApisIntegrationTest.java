package com.Amazon.clone;

import com.Amazon.clone.dto.Address.CreateAddressRequest;
import com.Amazon.clone.dto.Address.UpdateAddressRequest;
import com.Amazon.clone.dto.Auth.LoginRequest;
import com.Amazon.clone.dto.Auth.RegisterRequest;
import com.Amazon.clone.dto.User.UpdateProfileRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AllApisIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // Shared state between sequential ordered tests
    private static String userEmail;
    private static String userPassword = "Password123!";
    private static String jwtToken;
    private static String firstAddressId;
    private static String secondAddressId;

    // =========================================================================
    // 1. AUTH CONTROLLER TESTS (/api/v1/auth)
    // =========================================================================

    @Test
    @Order(1)
    void testRegisterUser_Success() throws Exception {
        userEmail = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        RegisterRequest request = RegisterRequest.builder()
                .fullName("Ahmed Ali")
                .email(userEmail)
                .password(userPassword)
                .phone("01012345678")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(userEmail))
                .andExpect(jsonPath("$.user.fullName").value("Ahmed Ali"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = responseNode.get("accessToken").asText();
        assertNotNull(jwtToken);
    }

    @Test
    @Order(2)
    void testRegisterUser_DuplicateEmail_Returns409() throws Exception {
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .fullName("Another Name")
                .email(userEmail) // Same email
                .password("Password123!")
                .phone("01099998888")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(3)
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(userEmail)
                .password(userPassword)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(userEmail))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = responseNode.get("accessToken").asText();
    }

    @Test
    @Order(4)
    void testLogin_InvalidPassword_Returns401() throws Exception {
        LoginRequest badRequest = LoginRequest.builder()
                .email(userEmail)
                .password("WrongPassword999")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // =========================================================================
    // 2. USER PROFILE TESTS (/api/v1/profile)
    // =========================================================================

    @Test
    @Order(5)
    void testGetProfile_Unauthorized_Returns403Or401() throws Exception {
        mockMvc.perform(get("/api/v1/profile/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void testGetProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.fullName").value("Ahmed Ali"))
                .andExpect(jsonPath("$.phone").value("01012345678"));
    }

    @Test
    @Order(7)
    void testUpdateProfile_Success() throws Exception {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullName("Ahmed Ali Updated")
                .phone("01087654321")
                .build();

        mockMvc.perform(put("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ahmed Ali Updated"))
                .andExpect(jsonPath("$.phone").value("01087654321"));
    }

    // =========================================================================
    // 3. ADDRESS TESTS (/api/v1/users/me/addresses)
    // =========================================================================

    @Test
    @Order(8)
    void testGetAddresses_InitiallyEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(9)
    void testCreateFirstAddress_BecomesDefaultAutomatically() throws Exception {
        CreateAddressRequest request = CreateAddressRequest.builder()
                .fullName("Ahmed Ali")
                .phone("01012345678")
                .addressLine("123 Nile Street")
                .addressLine2("Apartment 4B")
                .city("Cairo")
                .state("Cairo")
                .country("Egypt")
                .postalCode("11511")
                .isDefault(false) // Even if false, first address automatically defaults to true
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.city").value("Cairo"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        firstAddressId = responseNode.get("id").asText();
    }

    @Test
    @Order(10)
    void testCreateSecondAddress_NotDefault() throws Exception {
        CreateAddressRequest request = CreateAddressRequest.builder()
                .fullName("Ahmed Work")
                .phone("01099887766")
                .addressLine("456 Smart Village")
                .city("Giza")
                .country("Egypt")
                .postalCode("12577")
                .isDefault(false)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.city").value("Giza"))
                .andExpect(jsonPath("$.isDefault").value(false))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        secondAddressId = responseNode.get("id").asText();
    }

    @Test
    @Order(11)
    void testGetAddresses_ReturnsBoth() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(12)
    void testUpdateAddress_Success() throws Exception {
        UpdateAddressRequest updateRequest = UpdateAddressRequest.builder()
                .fullName("Ahmed Ali Residence")
                .phone("01011112222")
                .addressLine("999 Updated St")
                .city("Alexandria")
                .country("Egypt")
                .postalCode("21500")
                .build();

        mockMvc.perform(put("/api/v1/users/me/addresses/" + firstAddressId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Alexandria"))
                .andExpect(jsonPath("$.fullName").value("Ahmed Ali Residence"));
    }

    @Test
    @Order(13)
    void testSetDefaultAddress_Success() throws Exception {
        // Make second address default
        mockMvc.perform(patch("/api/v1/users/me/addresses/" + secondAddressId + "/default")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(secondAddressId))
                .andExpect(jsonPath("$.isDefault").value(true));

        // Verify first address is no longer default
        mockMvc.perform(get("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + secondAddressId + "')].isDefault").value(true))
                .andExpect(jsonPath("$[?(@.id == '" + firstAddressId + "')].isDefault").value(false));
    }

    @Test
    @Order(14)
    void testDeleteAddress_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me/addresses/" + secondAddressId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verify only 1 address remains
        mockMvc.perform(get("/api/v1/users/me/addresses")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
