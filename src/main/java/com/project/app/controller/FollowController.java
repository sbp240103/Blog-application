package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.ApiResponse;
import com.project.app.payloads.FollowStatsDto;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
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

    // users following this user
    @GetMapping("/followers")
    public ResponseEntity<PageResponse<UserDto>> getFollowers(
        @PathVariable("userId") Integer userId,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.followService.getFollowers(userId, pageNumber, pageSize));
    }

    // users this user follows
    @GetMapping("/following")
    public ResponseEntity<PageResponse<UserDto>> getFollowing(
        @PathVariable("userId") Integer userId,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.followService.getFollowing(userId, pageNumber, pageSize));
    }

    // follower and following counts
    @GetMapping("/follow-stats")
    public ResponseEntity<FollowStatsDto> getFollowStats(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(this.followService.getFollowStats(userId));
    }

}
