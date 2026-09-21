package com.project.app.services;

import com.project.app.payloads.CommentDto;

public interface CommentService {
    
    public CommentDto createComment(CommentDto commentDto , Integer postId );

    void deleteComment(Integer commentId);
}
