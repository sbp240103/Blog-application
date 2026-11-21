package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.app.entities.RefreshToken;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.RefreshTokenRepo;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepo refreshTokenRepo;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(this.refreshTokenService, "refreshTokenDays", 7L);
    }

    private User user() {
        User user = new User();
        user.setId(1);
        return user;
    }

    private RefreshToken token(String value, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setToken(value);
        token.setUser(user());
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void newTokenIsLongRandomAndExpiresInConfiguredDays() {
        when(this.refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = this.refreshTokenService.createRefreshToken(user());

        assertEquals(64, token.getToken().length());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));
        assertTrue(token.getExpiresAt().isBefore(LocalDateTime.now().plusDays(8)));
    }

    @Test
    void unknownTokenIsUnauthorized() {
        when(this.refreshTokenRepo.findByToken("nope")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> this.refreshTokenService.verify("nope"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void expiredTokenIsRemovedAndRejected() {
        RefreshToken expired = token("old", LocalDateTime.now().minusMinutes(1));
        when(this.refreshTokenRepo.findByToken("old")).thenReturn(Optional.of(expired));

        assertThrows(ApiException.class, () -> this.refreshTokenService.verify("old"));
        verify(this.refreshTokenRepo).delete(expired);
    }

    @Test
    void rotationBurnsTheOldTokenAndIssuesANewOne() {
        RefreshToken current = token("current", LocalDateTime.now().plusDays(3));
        when(this.refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken next = this.refreshTokenService.rotate(current);

        verify(this.refreshTokenRepo).delete(current);
        assertNotEquals("current", next.getToken());
    }

}
