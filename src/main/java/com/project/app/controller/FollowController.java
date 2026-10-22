package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.ApiResponse;
import com.project.app.services.FollowService;

@RestController
@RequestMapping("/api/users/{userId}")
public class FollowController {

    @Autowired
    private FollowService followService;

    // follow a user
    @PostMapping("/follow")
    public ResponseEntity<ApiResponse> followUser(@PathVariable("userId") Integer userId) {
        this.followService.followUser(userId);
        return ResponseEntity.ok(new ApiResponse("user followed", true));
    }

    // stop following a user
    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponse> unfollowUser(@PathVariable("userId") Integer userId) {
        this.followService.unfollowUser(userId);
        return ResponseEntity.ok(new ApiResponse("user unfollowed", true));
    }

}
