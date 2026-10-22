package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Follow;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.repositories.FollowRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.FollowService;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public void followUser(Integer userId) {
        User target = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        User me = this.authUtil.getLoggedInUser();

        if (me.getId() == target.getId()) {
            throw new ApiException("you can not follow yourself");
        }
        if (this.followRepo.existsByFollowerAndFollowing(me, target)) {
            throw new ApiException("you already follow this user", HttpStatus.CONFLICT);
        }
        this.followRepo.save(new Follow(me, target));
    }

    @Override
    public void unfollowUser(Integer userId) {
        User target = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        User me = this.authUtil.getLoggedInUser();

        Follow follow = this.followRepo.findByFollowerAndFollowing(me, target)
                .orElseThrow(() -> new ApiException("you do not follow this user"));
        this.followRepo.delete(follow);
    }

}
