package com.project.app.services;

import com.project.app.entities.NotificationType;
import com.project.app.entities.User;
import com.project.app.payloads.NotificationDto;
import com.project.app.payloads.PageResponse;

public interface NotificationService {

    void notify(User recipient, User actor, NotificationType type, Integer postId, String message);

    PageResponse<NotificationDto> getMyNotifications(Integer pageNumber, Integer pageSize);

    long getUnreadCount();

    NotificationDto markAsRead(Integer notificationId);

    int markAllAsRead();

}
