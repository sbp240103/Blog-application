package com.project.app.services;

import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;

public interface LikeService {

    void likePost(Integer postId);

    void unlikePost(Integer postId);

    PageResponse<UserDto> getUsersWhoLikedPost(Integer postId, Integer pageNumber, Integer pageSize);

}
