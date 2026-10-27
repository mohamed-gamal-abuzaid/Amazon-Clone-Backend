package com.Amazon.clone.service;

import com.Amazon.clone.dto.User.UpdateProfileRequest;
import com.Amazon.clone.dto.User.UserProfileResponse;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.exception.ResourceNotFoundException;
import com.Amazon.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ===========================
    //       GET PROFILE
    // ===========================
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildUserProfileResponse(user);
    }

    // ===========================
    //      UPDATE PROFILE
    // ===========================
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        userRepository.save(user);

        return buildUserProfileResponse(user);
    }

    // ===========================
    //         MAPPER
    // ===========================
    private UserProfileResponse buildUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
