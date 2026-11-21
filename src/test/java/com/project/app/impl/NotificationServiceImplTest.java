package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.project.app.entities.Notification;
import com.project.app.entities.NotificationType;
import com.project.app.entities.User;
import com.project.app.repositories.NotificationRepo;
import com.project.app.security.AuthUtil;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user(int id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    @Test
    void nobodyIsNotifiedAboutTheirOwnAction() {
        User me = user(1);
        this.notificationService.notify(me, me, NotificationType.LIKE, 3, "liked");
        verify(this.notificationRepo, never()).save(any());
    }

    @Test
    void missingRecipientIsIgnored() {
        this.notificationService.notify(null, user(1), NotificationType.COMMENT, 3, "commented");
        verify(this.notificationRepo, never()).save(any());
    }

    @Test
    void newNotificationIsStoredUnread() {
        this.notificationService.notify(user(1), user(2), NotificationType.FOLLOW, null, "followed you");

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(this.notificationRepo).save(saved.capture());
        assertEquals(NotificationType.FOLLOW, saved.getValue().getType());
        assertFalse(saved.getValue().isRead());
    }

    @Test
    void someoneElsesNotificationCanNotBeMarkedRead() {
        Notification notification = new Notification();
        notification.setId(4);
        notification.setRecipient(user(1));
        when(this.notificationRepo.findById(4)).thenReturn(Optional.of(notification));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(2));

        assertThrows(AccessDeniedException.class, () -> this.notificationService.markAsRead(4));
        verify(this.notificationRepo, never()).save(any());
    }

}
