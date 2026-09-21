package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.project.app.entities.Post;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.CommentDto;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.services.CommentService;

public class CommentServiceImpl implements CommentService {

    @Autowired 
    private PostRepo postRepo;

    @Autowired 
    private CommentRepo commentRepo;


	@Override
	public CommentDto createComment(CommentDto commentDto, Integer postId) {
		
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "post id", postId));
    }

	@Override
	public void deleteComment(Integer commentId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteComment'");
	}

}
