package com.project.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtTokenHelperTest {

    private JwtTokenHelper jwtTokenHelper;

    private final UserDetails alice = User.withUsername("alice@example.com").password("x").roles("USER").build();
    private final UserDetails bob = User.withUsername("bob@example.com").password("x").roles("USER").build();

    @BeforeEach
    void setUp() {
        this.jwtTokenHelper = new JwtTokenHelper();
        ReflectionTestUtils.setField(this.jwtTokenHelper, "secret", "unit-test-secret-key-that-is-long-enough-for-hs256");
        ReflectionTestUtils.setField(this.jwtTokenHelper, "tokenValidity", 60_000L);
    }

    @Test
    void tokenCarriesTheUsername() {
        String token = this.jwtTokenHelper.generateToken(this.alice);

        assertEquals("alice@example.com", this.jwtTokenHelper.getUsernameFromToken(token));
    }

    @Test
    void tokenIsValidOnlyForItsOwnUser() {
        String token = this.jwtTokenHelper.generateToken(this.alice);

        assertTrue(this.jwtTokenHelper.validateToken(token, this.alice));
        assertFalse(this.jwtTokenHelper.validateToken(token, this.bob));
    }

    @Test
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(this.jwtTokenHelper, "tokenValidity", -1_000L);
        String token = this.jwtTokenHelper.generateToken(this.alice);

        assertThrows(ExpiredJwtException.class, () -> this.jwtTokenHelper.getUsernameFromToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = this.jwtTokenHelper.generateToken(this.alice);
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThrows(JwtException.class, () -> this.jwtTokenHelper.getUsernameFromToken(tampered));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = this.jwtTokenHelper.generateToken(this.alice);
        ReflectionTestUtils.setField(this.jwtTokenHelper, "secret", "a-completely-different-secret-key-for-hs256-signing");

        assertThrows(JwtException.class, () -> this.jwtTokenHelper.getUsernameFromToken(token));
    }

}
