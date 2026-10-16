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

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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

    // change password of the logged in user
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        this.userService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse("password changed successfully", true));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") Integer uId){
        return ResponseEntity.ok(this.userService.getUserById(uId));
    }
}
