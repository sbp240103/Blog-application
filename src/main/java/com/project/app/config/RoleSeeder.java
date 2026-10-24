package com.project.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.project.app.entities.Role;
import com.project.app.entities.User;
import com.project.app.repositories.RoleRepo;
import com.project.app.repositories.UserRepo;

@Component
public class RoleSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // admin credentials only come from the environment, nothing is seeded when they are missing
    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:Administrator}")
    private String adminName;

    @Override
    public void run(String... args) throws Exception {

        if (!this.roleRepo.existsById(AppConstants.NORMAL_USER)) {
            this.roleRepo.save(new Role(AppConstants.NORMAL_USER, AppConstants.ROLE_NORMAL));
        }

        if (!this.roleRepo.existsById(AppConstants.ADMIN_USER)) {
            this.roleRepo.save(new Role(AppConstants.ADMIN_USER, AppConstants.ROLE_ADMIN));
        }

        this.seedAdmin();
    }

    private void seedAdmin() {
        if (this.adminEmail.isBlank() || this.adminPassword.isBlank()) {
            return;
        }

        Role normalRole = this.roleRepo.findById(AppConstants.NORMAL_USER).orElseThrow();
        Role adminRole = this.roleRepo.findById(AppConstants.ADMIN_USER).orElseThrow();

        User admin = this.userRepo.findByEmail(this.adminEmail).orElseGet(() -> {
            User user = new User();
            user.setName(this.adminName);
            user.setEmail(this.adminEmail);
            user.setPassword(this.passwordEncoder.encode(this.adminPassword));
            user.setAbout("platform administrator");
            return user;
        });

        // an existing account with this email simply gets the admin role added
        admin.getRoles().add(normalRole);
        admin.getRoles().add(adminRole);
        this.userRepo.save(admin);
    }

}
