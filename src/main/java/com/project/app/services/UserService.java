package com.project.app.services;

import java.util.List;

import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.UserDto;

public interface UserService {

    UserDto registerNewUser(UserDto user);
    UserDto createUser(UserDto user);
    UserDto updateUser(UserDto user, Integer userId);
    UserDto getUserById(Integer userId);
    UserDto getCurrentUser();
    void changePassword(ChangePasswordRequest request);
    List<UserDto> getAllUsers();
    void deleteUser(Integer userId);
}
