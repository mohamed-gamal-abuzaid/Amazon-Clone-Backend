package com.Amazon.clone.controller;


import com.Amazon.clone.dto.User.UpdateProfileRequest;
import com.Amazon.clone.dto.User.UserProfileResponse;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs for managing user profiles")
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get the currently authenticated user's profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UUID userId) {
        UserProfileResponse userProfileResponse = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfileResponse);
    }


    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update the currently authenticated user's profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@AuthenticationPrincipal UUID userId ,@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        UserProfileResponse userProfileResponse = userService.updateProfile(userId, updateProfileRequest);
        return ResponseEntity.ok(userProfileResponse);
    }
}
