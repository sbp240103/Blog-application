package com.project.app.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.RefreshToken;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.RefreshTokenRepo;
import com.project.app.services.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshTokenDays;

    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(this.refreshTokenDays));
        return this.refreshTokenRepo.save(refreshToken);
    }

    // an expired token is removed so it can not be tried again
    @Override
    public RefreshToken verify(String token) {
        RefreshToken refreshToken = this.refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new ApiException("invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (refreshToken.isExpired()) {
            this.refreshTokenRepo.delete(refreshToken);
            throw new ApiException("refresh token expired, please login again", HttpStatus.UNAUTHORIZED);
        }
        return refreshToken;
    }

    // every refresh hands out a new refresh token and burns the old one,
    // so a stolen token can only be used once
    @Override
    public RefreshToken rotate(RefreshToken current) {
        User user = current.getUser();
        this.refreshTokenRepo.delete(current);
        return this.createRefreshToken(user);
    }

    @Override
    public void revoke(String token) {
        this.refreshTokenRepo.findByToken(token).ifPresent(this.refreshTokenRepo::delete);
    }

}
