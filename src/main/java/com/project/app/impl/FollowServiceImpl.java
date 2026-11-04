package com.project.app.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Follow;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.FollowStatsDto;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.repositories.FollowRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.FollowService;
import com.project.app.utils.PageUtil;
import com.project.app.entities.NotificationType;
import com.project.app.services.NotificationService;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRepo followRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModelMapper modelMapper;

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
        this.notificationService.notify(target, me, NotificationType.FOLLOW, null, me.getName() + " started following you");
    }

    @Override
    public void unfollowUser(Integer userId) {
        User target = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        User me = this.authUtil.getLoggedInUser();

        Follow follow = this.followRepo.findByFollowerAndFollowing(me, target)
                .orElseThrow(() -> new ApiException("you do not follow this user"));
        this.followRepo.delete(follow);
    }

    @Override
    public PageResponse<UserDto> getFollowers(Integer userId, Integer pageNumber, Integer pageSize) {
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        Page<Follow> page = this.followRepo.findByFollowing(user, PageUtil.build(pageNumber, pageSize, "followedAt", "desc"));
        return PageResponse.of(page, follow -> this.modelMapper.map(follow.getFollower(), UserDto.class));
    }

    @Override
    public PageResponse<UserDto> getFollowing(Integer userId, Integer pageNumber, Integer pageSize) {
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        Page<Follow> page = this.followRepo.findByFollower(user, PageUtil.build(pageNumber, pageSize, "followedAt", "desc"));
        return PageResponse.of(page, follow -> this.modelMapper.map(follow.getFollowing(), UserDto.class));
    }

    @Override
    public FollowStatsDto getFollowStats(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        return new FollowStatsDto(user.getId(), this.followRepo.countByFollowing(user), this.followRepo.countByFollower(user));
    }

}
