package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.UserDto;
import com.project.app.services.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // give a user the admin role
    @PutMapping("/users/{userId}/promote")
    public ResponseEntity<UserDto> promoteToAdmin(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(this.adminService.promoteToAdmin(userId));
    }

    // take the admin role away from a user
    @PutMapping("/users/{userId}/demote")
    public ResponseEntity<UserDto> demoteFromAdmin(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(this.adminService.demoteFromAdmin(userId));
    }

}
