package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.app.entities.Notification;
import com.project.app.entities.NotificationType;
import com.project.app.entities.User;
import com.project.app.repositories.NotificationRepo;
import com.project.app.services.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

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

}
