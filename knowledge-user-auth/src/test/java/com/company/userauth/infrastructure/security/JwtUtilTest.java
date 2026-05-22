package com.company.userauth.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-for-jwt-util-unit-test-min-256-bits!!", 3600000L);
    }

    @Test
    void shouldGenerateTokenAndParseClaims() {
        String token = jwtUtil.generate(1L, "admin", "ADMIN");

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);

        Claims claims = jwtUtil.parse(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void shouldExtractUserIdFromClaims() {
        String token = jwtUtil.generate(42L, "user", "USER");
        Claims claims = jwtUtil.parse(token);

        Long userId = jwtUtil.getUserId(claims);
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void shouldContainExpirationInFuture() {
        String token = jwtUtil.generate(1L, "admin", "ADMIN");
        Claims claims = jwtUtil.parse(token);

        assertThat(claims.getExpiration()).isAfter(new java.util.Date());
    }

    @Test
    void shouldParseTokenWithCorrectIssuedAt() {
        long before = System.currentTimeMillis();
        String token = jwtUtil.generate(1L, "admin", "ADMIN");
        Claims claims = jwtUtil.parse(token);

        assertThat(claims.getIssuedAt().getTime()).isGreaterThanOrEqualTo(before - 1000);
    }
}
