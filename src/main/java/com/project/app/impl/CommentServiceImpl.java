package com.project.app.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.app.entities.Comments;
import com.project.app.entities.Post;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.CommentDto;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.services.CommentService;

@Service 
public class CommentServiceImpl implements CommentService {

    @Autowired 
    private PostRepo postRepo;

    @Autowired 
    private CommentRepo commentRepo;

    @Autowired 
    private ModelMapper modelMapper;

    // create
    @Override
    public CommentDto createComment(CommentDto commentDto, Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));

        Comments comment = this.modelMapper.map(commentDto, Comments.class);
        comment.setPost(post);

        Comments savedComment = this.commentRepo.save(comment);
        return this.modelMapper.map(savedComment, CommentDto.class);
    }

    // delete
    @Override
    public void deleteComment(Integer commentId) {
        Comments comment = this.commentRepo.findById(commentId).orElseThrow(()-> new ResouceNotFoundException("Comment", "comment id", commentId));
        this.commentRepo.delete(comment);
    }

}
