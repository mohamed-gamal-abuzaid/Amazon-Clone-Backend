package com.Amazon.clone.config;

import com.Amazon.clone.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. اقفل الـ CSRF — مش محتاجينه في REST API
                .csrf(AbstractHttpConfigurer::disable)

                // 2. اقفل الـ form login والـ httpBasic — بنستخدم JWT
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. خلي الـ sessions STATELESS — عشان بنستخدم JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. حدد مين يقدر يوصل لإيه
                .authorizeHttpRequests(auth -> auth
                        // الـ Auth endpoints — مفتوحة للكل
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Swagger & Static Player & HTML files — مفتوح للكل
                        .requestMatchers(
                                "/static/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Endpoints خاصة بالقناة محتاجة حماية (لازم تيجي قبل الـ GET العامة)
                        .requestMatchers("/api/v1/users/me/**").authenticated()
                        // الـ GET requests العامة — مفتوحة (مشاهدة بروفايل، بثوث، قنوات، فئات)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").permitAll()
                        // أي حاجة تانية — لازم يكون مسجل دخول
                        .anyRequest().authenticated()
                )

                // 4. ضيف الـ JWT Filter قبل الـ UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // === Password Encoder — BCrypt ===
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // === Authentication Manager ===
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
