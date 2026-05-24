package com.ask.security;

import com.ask.constants.ApiPaths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Spring Security configuration.
 * - Stateless JWT-based authentication (no server-side sessions)
 * - BCrypt password encoding with strength 12
 * - CSRF disabled (stateless API)
 * - Public endpoints: login, refresh, health check
 * - All other endpoints require authentication
 * - Custom 401/403 handlers that return our ApiResponse format
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Configures the security filter chain.
     * Every endpoint requires authentication except the explicitly permitted ones.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Stateless session management — no server-side sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no authentication needed
                        .requestMatchers(HttpMethod.POST,
                                ApiPaths.AUTH + ApiPaths.AUTH_LOGIN,
                                ApiPaths.AUTH + ApiPaths.AUTH_VERIFY_OTP,
                                ApiPaths.AUTH + ApiPaths.AUTH_RESEND_OTP,
                                ApiPaths.AUTH + ApiPaths.AUTH_REFRESH).permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Custom exception handling for 401 and 403 responses
                .exceptionHandling(ex -> ex
                        // 401 — when user is not authenticated
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            Map<String, Object> body = Map.of(
                                    "success", false,
                                    "message", "Authentication required. Please log in",
                                    "errorCode", "AUTHENTICATION_REQUIRED",
                                    "timestamp", LocalDateTime.now().toString(),
                                    "path", request.getRequestURI()
                            );
                            mapper.writeValue(response.getOutputStream(), body);
                        })
                        // 403 — when user is authenticated but lacks permission
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            Map<String, Object> body = Map.of(
                                    "success", false,
                                    "message", "You do not have permission to access this resource",
                                    "errorCode", "ACCESS_DENIED",
                                    "timestamp", LocalDateTime.now().toString(),
                                    "path", request.getRequestURI()
                            );
                            mapper.writeValue(response.getOutputStream(), body);
                        })
                )

                // Apply public auth endpoint rate limiting before JWT authentication
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder with strength 12.
     * Strength 12 provides a good balance between security and performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager bean — needed for the login flow.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
