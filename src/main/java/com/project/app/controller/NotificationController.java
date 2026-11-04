package com.project.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.ApiResponse;
import com.project.app.payloads.NotificationDto;
import com.project.app.payloads.PageResponse;
import com.project.app.services.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // notifications of the logged in user, newest first
    @GetMapping
    public ResponseEntity<PageResponse<NotificationDto>> getMyNotifications(
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.notificationService.getMyNotifications(pageNumber, pageSize));
    }

    // badge count for the bell icon
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unread", this.notificationService.getUnreadCount()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable("notificationId") Integer notificationId) {
        return ResponseEntity.ok(this.notificationService.markAsRead(notificationId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead() {
        int updated = this.notificationService.markAllAsRead();
        return ResponseEntity.ok(new ApiResponse(updated + " notification(s) marked as read", true));
    }

}
