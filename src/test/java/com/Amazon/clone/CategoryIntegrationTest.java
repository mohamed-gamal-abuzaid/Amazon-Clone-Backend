package com.Amazon.clone;

import com.Amazon.clone.dto.Category.CategoryRequest;
import com.Amazon.clone.dto.Category.CategoryUpdateRequest;
import com.Amazon.clone.entity.Role;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.entity.UserRole;
import com.Amazon.clone.repository.CategoryRepository;
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
class CategoryIntegrationTest {

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

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String suffix = UUID.randomUUID().toString().substring(0, 8);
    private static final String parentCategoryName = "Electronics_" + suffix;
    private static final String parentCategorySlug = "electronics-" + suffix;
    private static final String childCategoryName = "Smartphones_" + suffix;
    private static final String childCategorySlug = "smartphones-" + suffix;

    private static String adminToken;
    private static String userToken;
    private static String parentCategoryId;
    private static String childCategoryId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        if (adminToken == null) {
            // Seed ADMIN user
            String adminEmail = "cat_admin_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            User admin = new User();
            admin.setFullName("Admin Category");
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
            String userEmail = "cat_user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            User normalUser = new User();
            normalUser.setFullName("Normal Category User");
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
    void testCreateParentCategory_AsAdmin_Success() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name(parentCategoryName)
                .slug(parentCategorySlug)
                .description("All electronic devices")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(parentCategoryName))
                .andExpect(jsonPath("$.slug").value(parentCategorySlug))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        parentCategoryId = node.get("id").asText();
    }

    @Test
    @Order(2)
    void testCreateChildCategory_AsAdmin_Success() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name(childCategoryName)
                .slug(childCategorySlug)
                .description("Mobile phones and smartphones")
                .parentCategoryId(UUID.fromString(parentCategoryId))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(childCategoryName))
                .andExpect(jsonPath("$.parentCategoryId").value(parentCategoryId))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        childCategoryId = node.get("id").asText();
    }

    @Test
    @Order(3)
    void testCreateCategory_AsNormalUser_Forbidden() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Laptops_" + suffix)
                .slug("laptops-" + suffix)
                .description("Laptops and notebooks")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void testGetAllCategories_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.name == '" + parentCategoryName + "')].slug").value(parentCategorySlug));
    }

    @Test
    @Order(5)
    void testGetCategoryById_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/" + parentCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(parentCategoryId))
                .andExpect(jsonPath("$.name").value(parentCategoryName));
    }

    @Test
    @Order(6)
    void testGetChildrenOfCategory_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/" + parentCategoryId + "/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.id == '" + childCategoryId + "')].name").value(childCategoryName));
    }

    @Test
    @Order(7)
    void testUpdateCategory_AsNormalUser_Forbidden() throws Exception {
        CategoryUpdateRequest updateRequest = CategoryUpdateRequest.builder()
                .id(UUID.fromString(parentCategoryId))
                .name("Electronics Hacked")
                .build();

        mockMvc.perform(put("/api/v1/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    void testUpdateCategory_AsAdmin_Success() throws Exception {
        CategoryUpdateRequest updateRequest = CategoryUpdateRequest.builder()
                .id(UUID.fromString(parentCategoryId))
                .description("Updated electronics description")
                .build();

        mockMvc.perform(put("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated electronics description"));
    }

    @Test
    @Order(9)
    void testDeleteCategory_AsNormalUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/" + childCategoryId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(10)
    void testDeleteCategory_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/" + childCategoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify child is deleted
        mockMvc.perform(get("/api/v1/categories/" + childCategoryId))
                .andExpect(status().is4xxClientError());
    }
}
