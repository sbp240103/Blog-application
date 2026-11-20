package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

import com.project.app.entities.NotificationType;
import com.project.app.entities.Post;
import com.project.app.entities.PostLike;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.NotificationService;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private PostRepo postRepo;

    @Mock
    private PostLikeRepo postLikeRepo;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User user(int id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    private Post postOwnedBy(User owner) {
        Post post = new Post();
        post.setPostId(5);
        post.setTitle("A post");
        post.setUser(owner);
        return post;
    }

    @Test
    void likeIsSavedAndOwnerIsNotified() {
        User owner = user(1);
        User me = user(2);
        Post post = postOwnedBy(owner);
        when(this.postRepo.findById(5)).thenReturn(Optional.of(post));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.postLikeRepo.existsByUserAndPost(me, post)).thenReturn(false);

        this.likeService.likePost(5);

        verify(this.postLikeRepo).save(any(PostLike.class));
        verify(this.notificationService).notify(eq(owner), eq(me), eq(NotificationType.LIKE), eq(5), anyString());
    }

    @Test
    void likingTwiceIsAConflict() {
        User me = user(2);
        Post post = postOwnedBy(user(1));
        when(this.postRepo.findById(5)).thenReturn(Optional.of(post));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.postLikeRepo.existsByUserAndPost(me, post)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> this.likeService.likePost(5));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(this.postLikeRepo, never()).save(any());
    }

    @Test
    void unlikeWithoutALikeFails() {
        User me = user(2);
        Post post = postOwnedBy(user(1));
        when(this.postRepo.findById(5)).thenReturn(Optional.of(post));
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.postLikeRepo.findByUserAndPost(me, post)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> this.likeService.unlikePost(5));
        verify(this.postLikeRepo, never()).delete(any());
    }

}
