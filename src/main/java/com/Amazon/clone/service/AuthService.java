package com.Amazon.clone.service;

import com.Amazon.clone.dto.Auth.AuthResponse;
import com.Amazon.clone.dto.Auth.LoginRequest;
import com.Amazon.clone.dto.Auth.RegisterRequest;
import com.Amazon.clone.dto.Auth.UserResponse;
import com.Amazon.clone.entity.Role;
import com.Amazon.clone.entity.User;
import com.Amazon.clone.entity.UserRole;
import com.Amazon.clone.entity.enums.UserStatus;
import com.Amazon.clone.exception.EmailAlreadyExistsException;
import com.Amazon.clone.exception.InvalidCredentialsException;
import com.Amazon.clone.repository.RoleRepository;
import com.Amazon.clone.repository.UserRepository;
import com.Amazon.clone.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role is not configured"));
        UserRole userUserRole = new UserRole();
        userUserRole.setUser(user);
        userUserRole.setRole(userRole);
        user.getUserRoles().add(userUserRole);

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException("Account is not active or has been suspended");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String roleName = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User has no role"));

        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                roleName
        );

        UserResponse userDto = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProvider.getAccessExpiration())
                .user(userDto)
                .build();
    }
}
