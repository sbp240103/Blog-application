package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.app.entities.Notification;
import com.project.app.entities.NotificationType;
import com.project.app.entities.User;
import com.project.app.repositories.NotificationRepo;
import com.project.app.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.NotificationDto;
import com.project.app.payloads.PageResponse;
import com.project.app.security.AuthUtil;
import com.project.app.utils.PageUtil;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private AuthUtil authUtil;

    // nobody gets notified about their own actions
    @Override
    public void notify(User recipient, User actor, NotificationType type, Integer postId, String message) {
        if (recipient == null || actor == null || recipient.getId() == actor.getId()) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setPostId(postId);
        notification.setMessage(message);
        this.notificationRepo.save(notification);
    }

    // newest first
    @Override
    public PageResponse<NotificationDto> getMyNotifications(Integer pageNumber, Integer pageSize) {
        User me = this.authUtil.getLoggedInUser();
        Page<Notification> page = this.notificationRepo.findByRecipient(me, PageUtil.build(pageNumber, pageSize, "createdAt", "desc"));
        return PageResponse.of(page, this::toDto);
    }

    @Override
    public long getUnreadCount() {
        return this.notificationRepo.countByRecipientAndReadFalse(this.authUtil.getLoggedInUser());
    }

    @Override
    public NotificationDto markAsRead(Integer notificationId) {
        Notification notification = this.notificationRepo.findById(notificationId)
                .orElseThrow(() -> new ResouceNotFoundException("Notification", "Id", notificationId));
        if (notification.getRecipient().getId() != this.authUtil.getLoggedInUser().getId()) {
            throw new AccessDeniedException("this notification belongs to another user");
        }
        notification.setRead(true);
        return this.toDto(this.notificationRepo.save(notification));
    }

    @Override
    public int markAllAsRead() {
        return this.notificationRepo.markAllRead(this.authUtil.getLoggedInUser());
    }

    private NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setPostId(notification.getPostId());
        dto.setActorId(notification.getActor().getId());
        dto.setActorName(notification.getActor().getName());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

}
