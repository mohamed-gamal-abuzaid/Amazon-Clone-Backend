package com.Amazon.clone;

import com.Amazon.clone.dto.Auth.AuthResponse;
import com.Amazon.clone.dto.Auth.RegisterRequest;
import com.Amazon.clone.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CloneApplicationTests {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
    }

    @Test
    void testRegister() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("mohamed gamal")
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .password("12345678")
                .phone("01122503776")
                .build();
        AuthResponse response = authService.register(request);
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getUser().getId());
    }
}
