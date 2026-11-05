package com.project.app.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.app.repositories.RefreshTokenRepo;

// removes expired refresh tokens every night so the table does not keep growing
@Component
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Scheduled(cron = "${app.scheduler.token-cleanup-cron:0 0 3 * * *}")
    public void removeExpiredTokens() {
        int removed = this.refreshTokenRepo.deleteExpired(LocalDateTime.now());
        if (removed > 0) {
            log.info("removed {} expired refresh token(s)", removed);
        }
    }
}
