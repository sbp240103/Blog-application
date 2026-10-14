package com.project.app.services;

import java.util.List;

import com.project.app.payloads.CommentDto;

public interface CommentService {

    public CommentDto createComment(CommentDto commentDto , Integer postId );

    void deleteComment(Integer commentId);

    List<CommentDto> getCommentsByPost(Integer postId);
}
