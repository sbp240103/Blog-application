package com.project.app.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Post;
import com.project.app.entities.PostLike;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.LikeService;
import com.project.app.utils.PageUtil;
import com.project.app.entities.NotificationType;
import com.project.app.services.NotificationService;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void likePost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        if (this.postLikeRepo.existsByUserAndPost(user, post)) {
            throw new ApiException("you have already liked this post", HttpStatus.CONFLICT);
        }
        this.postLikeRepo.save(new PostLike(user, post));
        this.notificationService.notify(post.getUser(), user, NotificationType.LIKE, post.getPostId(),
                user.getName() + " liked your post \"" + post.getTitle() + "\"");
    }

    @Override
    public void unlikePost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        PostLike like = this.postLikeRepo.findByUserAndPost(user, post)
                .orElseThrow(() -> new ApiException("you have not liked this post"));
        this.postLikeRepo.delete(like);
    }

    // most recent likes first
    @Override
    public PageResponse<UserDto> getUsersWhoLikedPost(Integer postId, Integer pageNumber, Integer pageSize) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        Page<PostLike> likes = this.postLikeRepo.findByPost(post, PageUtil.build(pageNumber, pageSize, "likedAt", "desc"));
        return PageResponse.of(likes, like -> this.modelMapper.map(like.getUser(), UserDto.class));
    }

}
