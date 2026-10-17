package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Post;
import com.project.app.entities.PostLike;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.LikeService;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public void likePost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        if (this.postLikeRepo.existsByUserAndPost(user, post)) {
            throw new ApiException("you have already liked this post", HttpStatus.CONFLICT);
        }
        this.postLikeRepo.save(new PostLike(user, post));
    }

    @Override
    public void unlikePost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        PostLike like = this.postLikeRepo.findByUserAndPost(user, post)
                .orElseThrow(() -> new ApiException("you have not liked this post"));
        this.postLikeRepo.delete(like);
    }

}
