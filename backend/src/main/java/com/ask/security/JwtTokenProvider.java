package com.ask.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token provider responsible for generating, parsing, and validating JWT tokens.
 * Uses HMAC-SHA256 for signing. Access tokens are short-lived (15 min),
 * refresh tokens are long-lived (7 days).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    /**
     * Initialises the JWT provider with the secret key and expiry durations from properties.
     *
     * @param secret             the JWT signing secret (min 64 chars)
     * @param accessTokenExpiry  access token expiry in milliseconds
     * @param refreshTokenExpiry refresh token expiry in milliseconds
     */
    public JwtTokenProvider(
            @Value("${ask.jwt.secret}") String secret,
            @Value("${ask.jwt.access-token-expiry-ms}") long accessTokenExpiry,
            @Value("${ask.jwt.refresh-token-expiry-ms}") long refreshTokenExpiry) {
        // Generate the signing key from the secret string
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiry;
        this.refreshTokenExpiryMs = refreshTokenExpiry;
    }

    /**
     * Generates an access token for an authenticated user.
     * Contains the user's email as the subject and role as a claim.
     *
     * @param authentication the Spring Security authentication object
     * @return a signed JWT access token string
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(userDetails.getUsername()) // email is the username
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates an access token directly from an email (used in token refresh flow).
     *
     * @param email the user's email
     * @return a signed JWT access token string
     */
    public String generateAccessTokenFromEmail(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a refresh token for the given email.
     *
     * @param email the user's email
     * @return a signed JWT refresh token string
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiryMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .claim("type", "refresh") // Distinguish from access tokens
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return the email stored in the token's subject claim
     */
    public String getEmailFromToken(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * Validates a JWT token by attempting to parse it.
     * Catches and logs all specific JWT exception types.
     *
     * @param token the JWT token string to validate
     * @return true if the token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException e) {
            // Invalid JWT signature — someone tampered with the token
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // Token is not properly formatted
            log.error("Malformed JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // Token has expired — client should use refresh token
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // Token uses an unsupported algorithm or format
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token string is null or empty
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Returns the refresh token expiry duration in milliseconds.
     * Used when storing refresh tokens in the database.
     *
     * @return refresh token expiry in ms
     */
    public long getRefreshTokenExpiryMs() {
        return refreshTokenExpiryMs;
    }

    /**
     * Parses a JWT token string and returns the parsed JWT object.
     * This is the single point where all token parsing happens.
     *
     * @param token the JWT token string
     * @return parsed JWT object
     * @throws JwtException if parsing fails for any reason
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
