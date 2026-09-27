package com.project.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.app.entities.Role;
import com.project.app.repositories.RoleRepo;

@Component 
public class RoleSeeder implements CommandLineRunner {

    @Autowired 
    private RoleRepo roleRepo;

    @Override
    public void run(String... args) throws Exception {

        if (!this.roleRepo.existsById(AppConstants.NORMAL_USER)) {
            this.roleRepo.save(new Role(AppConstants.NORMAL_USER, AppConstants.ROLE_NORMAL));
        }

        if (!this.roleRepo.existsById(AppConstants.ADMIN_USER)) {
            this.roleRepo.save(new Role(AppConstants.ADMIN_USER, AppConstants.ROLE_ADMIN));
        }
    }

}
