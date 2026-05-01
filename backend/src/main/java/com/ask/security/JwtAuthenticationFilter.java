package com.ask.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that runs on every request.
 * Extracts the JWT token from the Authorization header, validates it,
 * loads the user details, and sets the authentication in the security context.
 *
 * Flow:
 * 1. Extract "Bearer <token>" from Authorization header
 * 2. Validate the token signature and expiry
 * 3. Load user details from database using the email in the token
 * 4. Check that the user account is still active
 * 5. Set authentication in SecurityContext so downstream code can access the user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract JWT token from the Authorization header
            String token = extractTokenFromRequest(request);

            // Step 2: Validate token and set authentication if valid
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // Step 3: Get the email from the token
                String email = jwtTokenProvider.getEmailFromToken(token);

                // Step 4: Load user details (also checks if account is active)
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create authentication token and set in context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log the error but don't block the request — let Spring Security handle 401
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        // Continue the filter chain regardless of auth result
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the "Authorization: Bearer <token>" header.
     *
     * @param request the HTTP request
     * @return the token string, or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
