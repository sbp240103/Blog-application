package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.app.entities.Post;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.PostDto;
import com.project.app.repositories.PostRepo;
import com.project.app.repositories.TagRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.FileService;
import com.project.app.utils.PostMapper;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepo postRepo;

    @Mock
    private TagRepo tagRepo;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private PostMapper postMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(this.postService, "imagePath", "images");
    }

    private User user(int id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private Post postOwnedBy(User owner) {
        Post post = new Post();
        post.setPostId(10);
        post.setTitle("A post");
        post.setUser(owner);
        post.setImageName("old.png");
        post.setViewCount(4);
        return post;
    }

    @Test
    void deletePostIsRejectedForNonOwner() {
        when(this.postRepo.findById(10)).thenReturn(Optional.of(postOwnedBy(user(1))));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(2));
        when(this.authUtil.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> this.postService.deletePost(10));
        verify(this.postRepo, never()).delete(any());
    }

    @Test
    void deletePostByOwnerRemovesPostAndItsImage() {
        User owner = user(1);
        Post post = postOwnedBy(owner);
        when(this.postRepo.findById(10)).thenReturn(Optional.of(post));
        when(this.authUtil.getLoggedInUser()).thenReturn(owner);

        this.postService.deletePost(10);

        verify(this.postRepo).delete(post);
        verify(this.fileService).deleteImage("images", "old.png");
    }

    @Test
    void adminCanDeleteAnyPost() {
        Post post = postOwnedBy(user(1));
        when(this.postRepo.findById(10)).thenReturn(Optional.of(post));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(2));
        when(this.authUtil.isAdmin()).thenReturn(true);

        this.postService.deletePost(10);

        verify(this.postRepo).delete(post);
    }

    @Test
    void getPostByIdCountsAView() {
        Post post = postOwnedBy(user(1));
        when(this.postRepo.findById(10)).thenReturn(Optional.of(post));
        when(this.postMapper.toDto(post)).thenReturn(new PostDto());

        this.postService.getPostById(10);

        verify(this.postRepo).incrementViewCount(10);
        assertEquals(5, post.getViewCount());
    }

    @Test
    void getPostByIdThrowsWhenMissing() {
        when(this.postRepo.findById(404)).thenReturn(Optional.empty());

        assertThrows(ResouceNotFoundException.class, () -> this.postService.getPostById(404));
        verify(this.postRepo, never()).incrementViewCount(any());
    }

    @Test
    void trendingRejectsInvalidLimit() {
        assertThrows(ApiException.class, () -> this.postService.getTrendingPosts(0));
        assertThrows(ApiException.class, () -> this.postService.getTrendingPosts(51));
    }

    @Test
    void searchNeedsAtLeastTwoCharacters() {
        assertThrows(ApiException.class, () -> this.postService.searchPosts("a", 0, 10, "postId", "desc"));
    }

    @Test
    void postsByUnknownTagIsNotFound() {
        when(this.tagRepo.findByName("nope")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> this.postService.getPostsByTag("Nope", 0, 10, "postId", "asc"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

}
