package com.project.app.services;

import com.project.app.entities.NotificationType;
import com.project.app.entities.User;

public interface NotificationService {

    void notify(User recipient, User actor, NotificationType type, Integer postId, String message);

}
