package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.project.app.entities.Follow;
import com.project.app.entities.NotificationType;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.FollowRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.NotificationService;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private FollowRepo followRepo;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowServiceImpl followService;

    private User user(int id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    @Test
    void youCanNotFollowYourself() {
        User me = user(1);
        when(this.userRepo.findById(1)).thenReturn(Optional.of(me));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);

        assertThrows(ApiException.class, () -> this.followService.followUser(1));
        verify(this.followRepo, never()).save(any());
    }

    @Test
    void followingTwiceIsAConflict() {
        User me = user(1);
        User target = user(2);
        when(this.userRepo.findById(2)).thenReturn(Optional.of(target));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.followRepo.existsByFollowerAndFollowing(me, target)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> this.followService.followUser(2));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void followSavesAndNotifiesTheFollowedUser() {
        User me = user(1);
        User target = user(2);
        when(this.userRepo.findById(2)).thenReturn(Optional.of(target));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.followRepo.existsByFollowerAndFollowing(me, target)).thenReturn(false);

        this.followService.followUser(2);

        verify(this.followRepo).save(any(Follow.class));
        verify(this.notificationService).notify(eq(target), eq(me), eq(NotificationType.FOLLOW), isNull(), anyString());
    }

    @Test
    void unfollowWithoutFollowingFails() {
        User me = user(1);
        User target = user(2);
        when(this.userRepo.findById(2)).thenReturn(Optional.of(target));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.followRepo.findByFollowerAndFollowing(me, target)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> this.followService.unfollowUser(2));
        verify(this.followRepo, never()).delete(any());
    }

}
