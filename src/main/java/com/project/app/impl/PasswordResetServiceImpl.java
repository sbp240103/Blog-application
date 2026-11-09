package com.project.app.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.app.entities.PasswordResetToken;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.PasswordResetTokenRepo;
import com.project.app.repositories.RefreshTokenRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.services.MailService;
import com.project.app.services.PasswordResetService;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_MINUTES = 30;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password?token=}")
    private String resetUrl;

    // silently does nothing for unknown emails so the endpoint can not be used to find accounts
    @Override
    @Transactional
    public void requestReset(String email) {
        this.userRepo.findByEmail(email).ifPresent(user -> {
            this.passwordResetTokenRepo.deleteByUser(user);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_MINUTES));
            this.passwordResetTokenRepo.save(resetToken);

            this.mailService.send(user.getEmail(), "Reset your password",
                    "Hi " + user.getName() + ",\n\nUse this link within " + TOKEN_MINUTES + " minutes to set a new password:\n"
                    + this.resetUrl + resetToken.getToken() + "\n\nIf you did not ask for this you can ignore this mail.");
        });
    }

    // a successful reset also unlocks the account and signs the user out everywhere
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = this.passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new ApiException("invalid or expired reset token"));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(this.passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        this.userRepo.save(user);

        resetToken.setUsed(true);
        this.passwordResetTokenRepo.save(resetToken);
        this.refreshTokenRepo.deleteByUser(user);
    }

}
