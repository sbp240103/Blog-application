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
import com.project.app.repositories.NotificationRepo;
import com.project.app.repositories.RefreshTokenRepo;
import com.project.app.repositories.PasswordResetTokenRepo;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.project.app.services.FileService;
import com.project.app.entities.PostStatus;
import com.project.app.payloads.ProfileDto;
import com.project.app.repositories.PostRepo;
import com.project.app.payloads.MyStatsDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import com.project.app.payloads.TopAuthorDto;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.data.domain.Sort;

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

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private FileService fileService;

    @Autowired
    private PostRepo postRepo;

    @Value("${project.image}")
    private String imagePath;

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
        user.setEnabled(true);
        user.setFailedLoginAttempts(0);
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
    public ProfileDto getProfile(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        return new ProfileDto(
                this.userToDto(user),
                this.postRepo.countByUserAndStatus(user, PostStatus.PUBLISHED),
                this.followRepo.countByFollowing(user),
                this.followRepo.countByFollower(user),
                this.postLikeRepo.countByPostUser(user));
    }

    // most followed users, banned accounts are skipped
    @Override
    public List<TopAuthorDto> getTopAuthors(Integer limit) {
        if (limit == null || limit < 1 || limit > 50) {
            throw new ApiException("limit must be between 1 and 50");
        }
        List<TopAuthorDto> result = new ArrayList<>();
        for (Object[] row : this.followRepo.findMostFollowed(PageRequest.of(0, limit * 2))) {
            User user = this.userRepo.findById((Integer) row[0]).orElse(null);
            if (user != null && !Boolean.FALSE.equals(user.getEnabled())) {
                result.add(new TopAuthorDto(this.userToDto(user), (Long) row[1]));
            }
            if (result.size() == limit) {
                break;
            }
        }
        return result;
    }

    // popular people the logged in user does not follow yet, topped up with newest members
    @Override
    public List<UserDto> getFollowSuggestions(Integer limit) {
        if (limit == null || limit < 1 || limit > 20) {
            throw new ApiException("limit must be between 1 and 20");
        }
        User me = this.authUtil.getLoggedInUser();
        Set<Integer> skip = new HashSet<>(this.followRepo.findFollowingIds(me));
        skip.add(me.getId());

        Set<Integer> candidates = new LinkedHashSet<>();
        for (Object[] row : this.followRepo.findMostFollowed(PageRequest.of(0, 50))) {
            candidates.add((Integer) row[0]);
        }
        this.userRepo.findAll(PageRequest.of(0, 50, Sort.by("id").descending())).forEach(user -> candidates.add(user.getId()));

        List<UserDto> suggestions = new ArrayList<>();
        for (Integer id : candidates) {
            if (suggestions.size() == limit) {
                break;
            }
            if (skip.contains(id)) {
                continue;
            }
            this.userRepo.findById(id)
                    .filter(user -> !Boolean.FALSE.equals(user.getEnabled()))
                    .ifPresent(user -> suggestions.add(this.userToDto(user)));
        }
        return suggestions;
    }

    // find people by name, used for the search box and mentions
    @Override
    public PageResponse<UserDto> searchUsers(String keyword, Integer pageNumber, Integer pageSize) {
        if (keyword == null || keyword.trim().length() < 2) {
            throw new ApiException("search keyword must have at least 2 characters");
        }
        Page<User> page = this.userRepo.findByNameContainingIgnoreCase(keyword.trim(), PageUtil.build(pageNumber, pageSize, "name", "asc"));
        return PageResponse.of(page, this::userToDto);
    }

    @Override
    public MyStatsDto getMyStats() {
        User me = this.authUtil.getLoggedInUser();
        return new MyStatsDto(
                this.postRepo.countByUserAndStatus(me, PostStatus.PUBLISHED),
                this.postRepo.countByUserAndStatus(me, PostStatus.DRAFT),
                this.postRepo.sumViewsByUser(me),
                this.postLikeRepo.countByPostUser(me),
                this.commentRepo.countByPostUser(me),
                this.followRepo.countByFollowing(me),
                this.followRepo.countByFollower(me),
                this.bookmarkRepo.countByUser(me));
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

    // replaces the profile picture and removes the old file
    @Override
    public UserDto uploadAvatar(MultipartFile image) throws IOException {
        User user = this.authUtil.getLoggedInUser();
        String oldAvatar = user.getAvatarImage();

        user.setAvatarImage(this.fileService.uploadImage(this.imagePath, image));
        User savedUser = this.userRepo.save(user);

        this.fileService.deleteImage(this.imagePath, oldAvatar);
        return this.userToDto(savedUser);
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
        this.notificationRepo.deleteByRecipientOrActor(user, user);
        this.refreshTokenRepo.deleteByUser(user);
        this.passwordResetTokenRepo.deleteByUser(user);

        String avatar = user.getAvatarImage();
        this.userRepo.delete(user);
        this.fileService.deleteImage(this.imagePath, avatar);
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
