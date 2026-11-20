package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;

import com.project.app.entities.Comments;
import com.project.app.entities.NotificationType;
import com.project.app.entities.Post;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.payloads.CommentDto;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.NotificationService;
import com.project.app.utils.ContentFilter;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private PostRepo postRepo;

    @Mock
    private CommentRepo commentRepo;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ContentFilter contentFilter;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user(int id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private Post postOwnedBy(User owner) {
        Post post = new Post();
        post.setPostId(7);
        post.setTitle("A post");
        post.setUser(owner);
        return post;
    }

    private Comments comment(int id, User author, Post post) {
        Comments comment = new Comments();
        comment.setId(id);
        comment.setContent("a comment");
        comment.setUser(author);
        comment.setPost(post);
        return comment;
    }

    private CommentDto dto(String content) {
        CommentDto dto = new CommentDto();
        dto.setContent(content);
        return dto;
    }

    @Test
    void createCommentRecordsAuthorAndNotifiesPostOwner() {
        User owner = user(1);
        User author = user(2);
        when(this.postRepo.findById(7)).thenReturn(Optional.of(postOwnedBy(owner)));
        when(this.authUtil.getLoggedInUser()).thenReturn(author);
        when(this.commentRepo.save(any(Comments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        this.commentService.createComment(dto("nice post"), 7);

        ArgumentCaptor<Comments> saved = ArgumentCaptor.forClass(Comments.class);
        verify(this.commentRepo).save(saved.capture());
        assertEquals(author, saved.getValue().getUser());
        verify(this.notificationService).notify(eq(owner), eq(author), eq(NotificationType.COMMENT), eq(7), anyString());
    }

    @Test
    void bannedContentIsNeverSaved() {
        when(this.postRepo.findById(7)).thenReturn(Optional.of(postOwnedBy(user(1))));
        doThrow(new ApiException("content contains words that are not allowed")).when(this.contentFilter).check("buy followers now");

        assertThrows(ApiException.class, () -> this.commentService.createComment(dto("buy followers now"), 7));
        verify(this.commentRepo, never()).save(any());
    }

    @Test
    void onlyTheAuthorCanEditAComment() {
        Comments comment = comment(3, user(2), postOwnedBy(user(1)));
        when(this.commentRepo.findById(3)).thenReturn(Optional.of(comment));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(1));

        assertThrows(AccessDeniedException.class, () -> this.commentService.updateComment(dto("edited"), 3));
        verify(this.commentRepo, never()).save(any());
    }

    @Test
    void postOwnerCanDeleteCommentsOnTheirPost() {
        User owner = user(1);
        Comments comment = comment(3, user(2), postOwnedBy(owner));
        when(this.commentRepo.findById(3)).thenReturn(Optional.of(comment));
        when(this.authUtil.getLoggedInUser()).thenReturn(owner);

        this.commentService.deleteComment(3);

        verify(this.commentRepo).delete(comment);
    }

    @Test
    void strangerCanNotDeleteAComment() {
        Comments comment = comment(3, user(2), postOwnedBy(user(1)));
        when(this.commentRepo.findById(3)).thenReturn(Optional.of(comment));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(9));
        when(this.authUtil.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> this.commentService.deleteComment(3));
        verify(this.commentRepo, never()).delete(any());
    }

    @Test
    void replyToAReplyIsAttachedToTheTopLevelComment() {
        Post post = postOwnedBy(user(1));
        Comments top = comment(10, user(1), post);
        Comments reply = comment(11, user(2), post);
        reply.setParent(top);
        when(this.commentRepo.findById(11)).thenReturn(Optional.of(reply));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(3));
        when(this.commentRepo.save(any(Comments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        this.commentService.createReply(dto("answering the reply"), 11);

        ArgumentCaptor<Comments> saved = ArgumentCaptor.forClass(Comments.class);
        verify(this.commentRepo).save(saved.capture());
        assertEquals(top, saved.getValue().getParent());
        assertEquals(post, saved.getValue().getPost());
    }

}
