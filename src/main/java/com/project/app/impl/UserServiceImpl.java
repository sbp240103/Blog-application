package com.project.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.app.payloads.UserDto;
import com.project.app.config.AppConstants;
import com.project.app.entities.Role;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.RoleRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.UserService;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired 
    private UserRepo userRepo;

    @Autowired 
    private ModelMapper modelMapper;

    @Autowired 
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CommentRepo commentRepo;

    @Override
    public UserDto registerNewUser(UserDto userDto) {
        return this.createUser(userDto);
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        if (this.userRepo.existsByEmail(userDto.getEmail())) {
            throw new ApiException("email is already registered : " + userDto.getEmail(), HttpStatus.CONFLICT);
        }

        User user = this.dtoToUser(userDto);
        user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));

        Role role = this.roleRepo.findById(AppConstants.NORMAL_USER)
                .orElseThrow(()-> new ResouceNotFoundException("Role", "Id", AppConstants.NORMAL_USER));
        user.getRoles().add(role);

        User savedUser = this.userRepo.save(user);

        return this.userToDto(savedUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user = this.userRepo.findById(userId)
                .orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(userDto.getEmail());
        if (emailChanged && this.userRepo.existsByEmail(userDto.getEmail())) {
            throw new ApiException("email is already registered : " + userDto.getEmail(), HttpStatus.CONFLICT);
        }

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setAbout(userDto.getAbout());
        user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));

        User updatedUser= this.userRepo.save(user);
        UserDto userDto1= this.userToDto(updatedUser);
        return userDto1;
    }

    @Override
    public UserDto getUserById(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        return this.userToDto(user);
    }

    @Override
    public UserDto getCurrentUser() {
        return this.userToDto(this.authUtil.getLoggedInUser());
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = this.userRepo.findAll();

        List<UserDto> userDtos = users.stream().map(user->this.userToDto(user)).collect(Collectors.toList());
        return userDtos;
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));

        // remove what this user wrote on other people's posts, own posts go with the user
        this.commentRepo.deleteByUser(user);

        this.userRepo.delete(user);
    }

    public  User dtoToUser(UserDto userDto){
        User user = this.modelMapper.map(userDto, User.class);
        // user.setId(userDto.getId());
        // user.setName(userDto.getName());
        // user.setEmail(userDto.getEmail());
        // user.setPassword(userDto.getPassword());
        // user.setAbout(userDto.getAbout());
        return user;
    }

    public  UserDto userToDto(User user){
        UserDto userDto = this.modelMapper.map(user, UserDto.class);
        return userDto;
    }

    
}
