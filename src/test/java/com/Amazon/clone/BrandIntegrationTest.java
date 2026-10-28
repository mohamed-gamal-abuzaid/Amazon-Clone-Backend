package com.Amazon.clone;

import com.Amazon.clone.dto.Brand.BrandRequest;
import com.Amazon.clone.dto.Brand.BrandUpdateRequest;
import com.Amazon.clone.entity.Role;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.entity.UserRole;
import com.Amazon.clone.repository.RoleRepository;
import com.Amazon.clone.repository.UserRepository;
import com.Amazon.clone.security.JwtProvider;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrandIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String adminToken;
    private static String userToken;
    private static String createdBrandId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        if (adminToken == null) {
            // Seed ADMIN user
            String adminEmail = "admin_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            User admin = new User();
            admin.setFullName("Admin User");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("AdminPass123!"));
            admin.setPhone("01011112222");

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            UserRole ur = new UserRole();
            ur.setUser(admin);
            ur.setRole(adminRole);
            admin.getUserRoles().add(ur);
            admin = userRepository.save(admin);

            adminToken = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), "ADMIN");

            // Seed regular USER
            String userEmail = "normal_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            User normalUser = new User();
            normalUser.setFullName("Normal User");
            normalUser.setEmail(userEmail);
            normalUser.setPasswordHash(passwordEncoder.encode("UserPass123!"));
            normalUser.setPhone("01033334444");

            Role userRole = roleRepository.findByName("USER").orElseThrow();
            UserRole ur2 = new UserRole();
            ur2.setUser(normalUser);
            ur2.setRole(userRole);
            normalUser.getUserRoles().add(ur2);
            normalUser = userRepository.save(normalUser);

            userToken = jwtProvider.generateAccessToken(normalUser.getId(), normalUser.getEmail(), "USER");
        }
    }

    @Test
    @Order(1)
    void testCreateBrand_AsAdmin_Success() throws Exception {
        BrandRequest request = BrandRequest.builder()
                .name("Apple")
                .slug("apple")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Apple"))
                .andExpect(jsonPath("$.slug").value("apple"))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        createdBrandId = node.get("id").asText();
    }

    @Test
    @Order(2)
    void testCreateBrand_AsNormalUser_Forbidden() throws Exception {
        BrandRequest request = BrandRequest.builder()
                .name("Samsung")
                .slug("samsung")
                .build();

        mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void testGetAllBrands_Public_Success() throws Exception {
        // Public request without token
        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.name == 'Apple')].slug").value("apple"));
    }

    @Test
    @Order(4)
    void testGetBrandById_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/brands/" + createdBrandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBrandId))
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    @Order(5)
    void testGetBrandByName_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/brands/name/Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBrandId))
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    @Order(6)
    void testUpdateBrand_AsNormalUser_Forbidden() throws Exception {
        BrandUpdateRequest updateRequest = BrandUpdateRequest.builder()
                .id(UUID.fromString(createdBrandId))
                .name("Apple Hacked")
                .slug("apple-hacked")
                .build();

        mockMvc.perform(put("/api/v1/brands")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void testUpdateBrand_AsAdmin_Success() throws Exception {
        BrandUpdateRequest updateRequest = BrandUpdateRequest.builder()
                .id(UUID.fromString(createdBrandId))
                .name("Apple Inc.")
                .slug("apple-inc")
                .build();

        mockMvc.perform(put("/api/v1/brands")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple Inc."))
                .andExpect(jsonPath("$.slug").value("apple-inc"));
    }

    @Test
    @Order(8)
    void testDeleteBrand_AsNormalUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/brands/" + createdBrandId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void testDeleteBrand_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/brands/" + createdBrandId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify it was deleted (calling it again should return 404)
        mockMvc.perform(get("/api/v1/brands/" + createdBrandId))
                .andExpect(status().isNotFound());
    }
}
