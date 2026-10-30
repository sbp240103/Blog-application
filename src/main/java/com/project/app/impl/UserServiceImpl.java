package com.project.app.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.utils.PageUtil;
import com.project.app.config.AppConstants;
import com.project.app.entities.Role;
import com.project.app.repositories.BookmarkRepo;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.FollowRepo;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.RoleRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.UserService;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.UpdateProfileRequest;

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

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private BookmarkRepo bookmarkRepo;

    @Autowired
    private FollowRepo followRepo;

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

        // users can only edit their own account, admins can edit anyone
        User loggedInUser = this.authUtil.getLoggedInUser();
        if (loggedInUser.getId() != user.getId() && !this.authUtil.isAdmin()) {
            throw new AccessDeniedException("you can only update your own account");
        }

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

    // partial update of the logged in user, password and email are not touched here
    @Override
    public UserDto updateProfile(UpdateProfileRequest request) {
        if (request.getName() == null && request.getAbout() == null) {
            throw new ApiException("nothing to update, send name and or about");
        }

        User user = this.authUtil.getLoggedInUser();
        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getAbout() != null) {
            user.setAbout(request.getAbout().trim());
        }
        return this.userToDto(this.userRepo.save(user));
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = this.authUtil.getLoggedInUser();

        if (!this.passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException("old password is incorrect");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new ApiException("new password must be different from the old password");
        }

        user.setPassword(this.passwordEncoder.encode(request.getNewPassword()));
        this.userRepo.save(user);
    }

    @Override
    public PageResponse<UserDto> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Page<User> page = this.userRepo.findAll(PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.USER_SORT_FIELDS));
        return PageResponse.of(page, this::userToDto);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));

        // remove what this user wrote on other people's posts, own posts go with the user
        this.commentRepo.deleteByUser(user);
        this.postLikeRepo.deleteByUser(user);
        this.bookmarkRepo.deleteByUser(user);
        this.followRepo.deleteByFollowerOrFollowing(user, user);

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
