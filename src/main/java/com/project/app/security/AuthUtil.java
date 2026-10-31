package com.project.app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.project.app.config.AppConstants;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.UserRepo;

// small helper to find out who is making the current request
@Component
public class AuthUtil {

    @Autowired
    private UserRepo userRepo;

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ApiException("you need to login first", HttpStatus.UNAUTHORIZED);
        }

        return this.userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ApiException("logged in user no longer exists", HttpStatus.UNAUTHORIZED));
    }

    // same as getLoggedInUser but gives null for anonymous requests instead of failing
    public User getLoggedInUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return this.userRepo.findByEmail(authentication.getName()).orElse(null);
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> AppConstants.ROLE_ADMIN.equals(authority.getAuthority()));
    }

}
