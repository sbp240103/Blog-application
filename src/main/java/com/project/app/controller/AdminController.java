package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.StatsDto;
import com.project.app.payloads.UserDto;
import com.project.app.services.AdminService;
import com.project.app.payloads.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.app.config.AppConstants;
import com.project.app.entities.ReportStatus;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.ReportDto;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // platform wide counts for a dashboard
    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats() {
        return ResponseEntity.ok(this.adminService.getStats());
    }

    // stop a user from logging in
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse> banUser(@PathVariable("userId") Integer userId) {
        this.adminService.banUser(userId);
        return ResponseEntity.ok(new ApiResponse("user banned", true));
    }

    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<ApiResponse> unbanUser(@PathVariable("userId") Integer userId) {
        this.adminService.unbanUser(userId);
        return ResponseEntity.ok(new ApiResponse("user unbanned", true));
    }

    // show a post on the home page
    @PutMapping("/posts/{postId}/feature")
    public ResponseEntity<ApiResponse> featurePost(@PathVariable("postId") Integer postId) {
        this.adminService.setPostFeatured(postId, true);
        return ResponseEntity.ok(new ApiResponse("post featured", true));
    }

    @PutMapping("/posts/{postId}/unfeature")
    public ResponseEntity<ApiResponse> unfeaturePost(@PathVariable("postId") Integer postId) {
        this.adminService.setPostFeatured(postId, false);
        return ResponseEntity.ok(new ApiResponse("post removed from featured", true));
    }

    // moderation queue, open reports by default
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportDto>> getReports(
        @RequestParam(value = "status", defaultValue = "OPEN", required = false) ReportStatus status,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.adminService.getReports(status, pageNumber, pageSize));
    }

    // close a report, optionally removing the reported content
    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ReportDto> resolveReport(
        @PathVariable("reportId") Integer reportId,
        @RequestParam(value = "removeContent", defaultValue = "false", required = false) boolean removeContent
    ) {
        return ResponseEntity.ok(this.adminService.resolveReport(reportId, removeContent));
    }

    @PutMapping("/reports/{reportId}/dismiss")
    public ResponseEntity<ReportDto> dismissReport(@PathVariable("reportId") Integer reportId) {
        return ResponseEntity.ok(this.adminService.dismissReport(reportId));
    }

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
