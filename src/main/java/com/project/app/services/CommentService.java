package com.project.app.services;

import java.util.List;

import com.project.app.payloads.CommentDto;
import com.project.app.payloads.PageResponse;

public interface CommentService {

    public CommentDto createComment(CommentDto commentDto , Integer postId );

    CommentDto createReply(CommentDto commentDto, Integer commentId);

    List<CommentDto> getReplies(Integer commentId);

    CommentDto updateComment(CommentDto commentDto, Integer commentId);

    void deleteComment(Integer commentId);

    PageResponse<CommentDto> getCommentsByPost(Integer postId, Integer pageNumber, Integer pageSize);
}
