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

    // create
    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));

        Comments comment = this.modelMapper.map(commentDto, Comments.class);
        comment.setPost(post);
        comment.setUser(this.authUtil.getLoggedInUser());

        Comments savedComment = this.commentRepo.save(comment);
        return this.modelMapper.map(savedComment, CommentDto.class);
    }

    // only the author can edit a comment
    @Override
    public CommentDto updateComment(CommentDto commentDto, Integer commentId) {
        Comments comment = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));

        User loggedInUser = this.authUtil.getLoggedInUser();
        if (comment.getUser() == null || comment.getUser().getId() != loggedInUser.getId()) {
            throw new AccessDeniedException("only the author can edit this comment");
        }

        comment.setContent(commentDto.getContent());
        Comments updatedComment = this.commentRepo.save(comment);
        return this.modelMapper.map(updatedComment, CommentDto.class);
    }

    // all comments of a post, oldest first
    @Override
    public List<CommentDto> getCommentsByPost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));
        return this.commentRepo.findByPostOrderByIdAsc(post).stream()
                .map(comment -> this.modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
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
