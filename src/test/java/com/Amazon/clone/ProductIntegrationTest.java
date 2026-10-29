package com.Amazon.clone;

import com.Amazon.clone.dto.Product.*;
import com.Amazon.clone.entity.*;
import com.Amazon.clone.entity.enums.ProductStatus;
import com.Amazon.clone.repository.*;
import com.Amazon.clone.security.JwtProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

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
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String suffix = UUID.randomUUID().toString().substring(0, 8);
    private static final String productName = "iPhone 16 Pro " + suffix;
    private static final String productSlug = "iphone-16-pro-" + suffix;
    private static final String variantSku = "IPHONE-16-PRO-256-BLK-" + suffix;

    private static String adminToken;
    private static String userToken;
    private static UUID testBrandId;
    private static UUID testCategoryId;
    private static String createdProductId;
    private static String createdVariantId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        if (adminToken == null) {
            // Seed ADMIN user
            String adminEmail = "prod_admin_" + suffix + "@example.com";
            User admin = new User();
            admin.setFullName("Admin Product");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("AdminPass123!"));
            admin.setPhone("01011119999");

            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            UserRole ur = new UserRole();
            ur.setUser(admin);
            ur.setRole(adminRole);
            admin.getUserRoles().add(ur);
            admin = userRepository.save(admin);

            adminToken = jwtProvider.generateAccessToken(admin.getId(), admin.getEmail(), "ADMIN");

            // Seed regular USER
            String userEmail = "prod_user_" + suffix + "@example.com";
            User normalUser = new User();
            normalUser.setFullName("Normal Product User");
            normalUser.setEmail(userEmail);
            normalUser.setPasswordHash(passwordEncoder.encode("UserPass123!"));
            normalUser.setPhone("01033338888");

            Role userRole = roleRepository.findByName("USER").orElseThrow();
            UserRole ur2 = new UserRole();
            ur2.setUser(normalUser);
            ur2.setRole(userRole);
            normalUser.getUserRoles().add(ur2);
            normalUser = userRepository.save(normalUser);

            userToken = jwtProvider.generateAccessToken(normalUser.getId(), normalUser.getEmail(), "USER");

            // Seed Brand & Category
            Brand brand = new Brand();
            brand.setName("Apple " + suffix);
            brand.setSlug("apple-" + suffix);
            brand = brandRepository.save(brand);
            testBrandId = brand.getId();

            Category category = new Category();
            category.setName("Mobiles " + suffix);
            category.setSlug("mobiles-" + suffix);
            category = categoryRepository.save(category);
            testCategoryId = category.getId();
        }
    }

    @Test
    @Order(1)
    void testCreateProduct_AsAdmin_Success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name(productName)
                .slug(productSlug)
                .description("Latest Apple flagship smartphone")
                .brandId(testBrandId)
                .categoryIds(Set.of(testCategoryId))
                .status(ProductStatus.ACTIVE)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(productName))
                .andExpect(jsonPath("$.slug").value(productSlug))
                .andExpect(jsonPath("$.brand.id").value(testBrandId.toString()))
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        createdProductId = node.get("id").asText();
    }

    @Test
    @Order(2)
    void testCreateProduct_AsNormalUser_Forbidden() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Samsung Galaxy " + suffix)
                .slug("samsung-galaxy-" + suffix)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void testAddVariant_AsAdmin_Success() throws Exception {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("color", "Titanium Black");
        attributes.put("storage", "256GB");

        CreateVariantRequest request = CreateVariantRequest.builder()
                .sku(variantSku)
                .attributes(attributes)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/products/" + createdProductId + "/variants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.productId").value(createdProductId))
                .andExpect(jsonPath("$.sku").value(variantSku))
                .andExpect(jsonPath("$.attributes.color").value("Titanium Black"))
                .andExpect(jsonPath("$.attributes.storage").value("256GB"))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        createdVariantId = node.get("id").asText();
    }

    @Test
    @Order(4)
    void testAddVariant_AsNormalUser_Forbidden() throws Exception {
        CreateVariantRequest request = CreateVariantRequest.builder()
                .sku("ILLEGAL-SKU-" + suffix)
                .attributes(Map.of("color", "Red"))
                .build();

        mockMvc.perform(post("/api/v1/products/" + createdProductId + "/variants")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void testAddImage_AsAdmin_Success() throws Exception {
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .imageUrl("https://example.com/images/iphone-black.png")
                .altText("iPhone 16 Pro Black front view")
                .sortOrder(1)
                .build();

        mockMvc.perform(post("/api/v1/products/" + createdProductId + "/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/images/iphone-black.png"))
                .andExpect(jsonPath("$.sortOrder").value(1));
    }

    @Test
    @Order(6)
    void testGetAllProducts_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[?(@.name == '" + productName + "')].slug").value(productSlug));
    }

    @Test
    @Order(7)
    void testGetProductById_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + createdProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProductId))
                .andExpect(jsonPath("$.name").value(productName))
                .andExpect(jsonPath("$.variants", hasSize(1)))
                .andExpect(jsonPath("$.images", hasSize(1)))
                .andExpect(jsonPath("$.categories", hasSize(1)));
    }

    @Test
    @Order(8)
    void testGetProductBySlug_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/slug/" + productSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProductId))
                .andExpect(jsonPath("$.name").value(productName));
    }

    @Test
    @Order(9)
    void testUpdateProduct_AsAdmin_Success() throws Exception {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .description("Updated Apple smartphone description with AI features")
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/v1/products/" + createdProductId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Apple smartphone description with AI features"));
    }

    @Test
    @Order(10)
    void testUpdateProduct_AsNormalUser_Forbidden() throws Exception {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Hacked Product")
                .build();

        mockMvc.perform(put("/api/v1/products/" + createdProductId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    void testDeleteProduct_AsNormalUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + createdProductId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void testDeleteProduct_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + createdProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify product is gone
        mockMvc.perform(get("/api/v1/products/" + createdProductId))
                .andExpect(status().isNotFound());
    }
}
