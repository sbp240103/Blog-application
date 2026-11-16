package com.project.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.app.entities.Comments;
import com.project.app.entities.Post;
import com.project.app.entities.User;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.CommentDto;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.CommentService;
import org.springframework.data.domain.Page;
import com.project.app.payloads.PageResponse;
import com.project.app.utils.PageUtil;
import com.project.app.entities.NotificationType;
import com.project.app.services.NotificationService;
import com.project.app.utils.ContentFilter;

@Service 
public class CommentServiceImpl implements CommentService {

    @Autowired 
    private PostRepo postRepo;

    @Autowired 
    private CommentRepo commentRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ContentFilter contentFilter;

    // create
    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));

        this.contentFilter.check(commentDto.getContent());

        Comments comment = new Comments();
        comment.setContent(commentDto.getContent());
        comment.setPost(post);
        comment.setUser(this.authUtil.getLoggedInUser());

        Comments savedComment = this.commentRepo.save(comment);
        this.notificationService.notify(post.getUser(), savedComment.getUser(), NotificationType.COMMENT, post.getPostId(),
                savedComment.getUser().getName() + " commented on your post \"" + post.getTitle() + "\"");
        return this.modelMapper.map(savedComment, CommentDto.class);
    }

    // replying to a reply attaches to the top level comment so threads stay one level deep
    @Override
    public CommentDto createReply(CommentDto commentDto, Integer commentId) {
        Comments target = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));
        Comments parent = target.getParent() != null ? target.getParent() : target;

        this.contentFilter.check(commentDto.getContent());

        Comments reply = new Comments();
        reply.setContent(commentDto.getContent());
        reply.setPost(parent.getPost());
        reply.setParent(parent);
        reply.setUser(this.authUtil.getLoggedInUser());

        Comments savedReply = this.commentRepo.save(reply);
        this.notificationService.notify(parent.getUser(), savedReply.getUser(), NotificationType.REPLY, parent.getPost().getPostId(),
                savedReply.getUser().getName() + " replied to your comment");
        return this.modelMapper.map(savedReply, CommentDto.class);
    }

    // replies of a comment, oldest first
    @Override
    public List<CommentDto> getReplies(Integer commentId) {
        Comments parent = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));
        return this.commentRepo.findByParentOrderByIdAsc(parent).stream()
                .map(reply -> this.modelMapper.map(reply, CommentDto.class))
                .collect(Collectors.toList());
    }

    // only the author can edit a comment
    @Override
    public CommentDto updateComment(CommentDto commentDto, Integer commentId) {
        Comments comment = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));

        User loggedInUser = this.authUtil.getLoggedInUser();
        if (comment.getUser() == null || comment.getUser().getId() != loggedInUser.getId()) {
            throw new AccessDeniedException("only the author can edit this comment");
        }

        this.contentFilter.check(commentDto.getContent());
        comment.setContent(commentDto.getContent());
        Comments updatedComment = this.commentRepo.save(comment);
        return this.modelMapper.map(updatedComment, CommentDto.class);
    }

    // top level comments of a post, oldest first, replies are fetched separately
    @Override
    public PageResponse<CommentDto> getCommentsByPost(Integer postId, Integer pageNumber, Integer pageSize) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));
        Page<Comments> page = this.commentRepo.findByPostAndParentIsNull(post, PageUtil.build(pageNumber, pageSize, "Id", "asc"));
        return PageResponse.of(page, this::toDtoWithReplyCount);
    }

    private CommentDto toDtoWithReplyCount(Comments comment) {
        CommentDto dto = this.modelMapper.map(comment, CommentDto.class);
        dto.setReplyCount(this.commentRepo.countByParent(comment));
        return dto;
    }

    // delete
    @Override
    public void deleteComment(Integer commentId) {
        Comments comment = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));

        // the author, the owner of the post or an admin can remove a comment
        User loggedInUser = this.authUtil.getLoggedInUser();
        boolean isAuthor = comment.getUser() != null && comment.getUser().getId() == loggedInUser.getId();
        boolean isPostOwner = comment.getPost().getUser() != null && comment.getPost().getUser().getId() == loggedInUser.getId();
        if (!isAuthor && !isPostOwner && !this.authUtil.isAdmin()) {
            throw new AccessDeniedException("you can not delete this comment");
        }

        this.commentRepo.delete(comment);
    }

}
