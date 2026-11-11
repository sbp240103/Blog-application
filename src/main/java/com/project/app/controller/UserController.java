package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;

import jakarta.validation.Valid;
import com.project.app.services.UserService;
import com.project.app.payloads.ApiResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import com.project.app.payloads.UpdateProfileRequest;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import com.project.app.services.FileService;
import jakarta.servlet.http.HttpServletResponse;
import com.project.app.payloads.ProfileDto;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String imagePath;

    // create user
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = this.userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // update user

    @PutMapping ("/{userId}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable ("userId") Integer uId){
        UserDto updatedUser = this.userService.updateUser(userDto, uId);
        return ResponseEntity.ok(updatedUser);
    }

    // delete user

    @DeleteMapping ("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable("userId") Integer uId){
        this.userService.deleteUser(uId);
        return new ResponseEntity<>(new ApiResponse("user deleted successfully", true), HttpStatus.OK);
    }

    // get user
    @GetMapping
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "id", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection));
    }

    // currently logged in user
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(){
        return ResponseEntity.ok(this.userService.getCurrentUser());
    }

    // update name or about of the logged in user without resending the password
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request){
        return ResponseEntity.ok(this.userService.updateProfile(request));
    }

    // upload a profile picture, multipart field name is image
    @PostMapping("/me/avatar")
    public ResponseEntity<UserDto> uploadAvatar(@RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(this.userService.uploadAvatar(image));
    }

    // serve a profile picture
    @GetMapping("/avatar/{imageName}")
    public void downloadAvatar(@PathVariable("imageName") String imageName, HttpServletResponse response) throws IOException {
        try (InputStream resource = this.fileService.getResource(this.imagePath, imageName)) {
            boolean isPng = imageName.toLowerCase().endsWith(".png");
            response.setContentType(isPng ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE);
            StreamUtils.copy(resource, response.getOutputStream());
        }
    }

    // change password of the logged in user
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        this.userService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse("password changed successfully", true));
    }

    // public profile with post, follower and like counts
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable("userId") Integer userId){
        return ResponseEntity.ok(this.userService.getProfile(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") Integer uId){
        return ResponseEntity.ok(this.userService.getUserById(uId));
    }
}
