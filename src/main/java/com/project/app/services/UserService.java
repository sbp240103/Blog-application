package com.project.app.services;

import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.payloads.UpdateProfileRequest;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.project.app.payloads.ProfileDto;
import com.project.app.payloads.MyStatsDto;
import java.util.List;
import com.project.app.payloads.TopAuthorDto;
import com.project.app.payloads.DataExportDto;

public interface UserService {

    UserDto registerNewUser(UserDto user);
    UserDto createUser(UserDto user);
    UserDto updateUser(UserDto user, Integer userId);
    UserDto getUserById(Integer userId);
    UserDto getCurrentUser();
    MyStatsDto getMyStats();
    DataExportDto exportMyData();
    ProfileDto getProfile(Integer userId);
    List<TopAuthorDto> getTopAuthors(Integer limit);
    List<UserDto> getFollowSuggestions(Integer limit);
    PageResponse<UserDto> searchUsers(String keyword, Integer pageNumber, Integer pageSize);
    void changePassword(ChangePasswordRequest request);
    UserDto updateProfile(UpdateProfileRequest request);
    UserDto uploadAvatar(MultipartFile image) throws IOException;
    PageResponse<UserDto> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    void deleteUser(Integer userId);
}
