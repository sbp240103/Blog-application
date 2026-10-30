package com.project.app.services;

import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.payloads.UpdateProfileRequest;

public interface UserService {

    UserDto registerNewUser(UserDto user);
    UserDto createUser(UserDto user);
    UserDto updateUser(UserDto user, Integer userId);
    UserDto getUserById(Integer userId);
    UserDto getCurrentUser();
    void changePassword(ChangePasswordRequest request);
    UserDto updateProfile(UpdateProfileRequest request);
    PageResponse<UserDto> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    void deleteUser(Integer userId);
}
