package com.project.app.services;

import com.project.app.payloads.FollowStatsDto;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;

public interface FollowService {

    void followUser(Integer userId);

    void unfollowUser(Integer userId);

    PageResponse<UserDto> getFollowers(Integer userId, Integer pageNumber, Integer pageSize);

    PageResponse<UserDto> getFollowing(Integer userId, Integer pageNumber, Integer pageSize);

    FollowStatsDto getFollowStats(Integer userId);

}
