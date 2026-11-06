package com.project.app.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.app.repositories.UserRepo;
import com.project.app.services.LoginAttemptService;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Autowired
    private UserRepo userRepo;

    @Value("${app.security.max-failed-logins:5}")
    private int maxFailedLogins;

    @Value("${app.security.lock-minutes:15}")
    private int lockMinutes;

    @Override
    public void loginFailed(String email) {
        this.userRepo.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= this.maxFailedLogins) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(this.lockMinutes));
                attempts = 0;
            }
            user.setFailedLoginAttempts(attempts);
            this.userRepo.save(user);
        });
    }

    @Override
    public void loginSucceeded(String email) {
        this.userRepo.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                this.userRepo.save(user);
            }
        });
    }

}
