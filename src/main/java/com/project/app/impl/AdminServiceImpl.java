package com.project.app.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.config.AppConstants;
import com.project.app.entities.Role;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.StatsDto;
import com.project.app.payloads.UserDto;
import com.project.app.repositories.BookmarkRepo;
import com.project.app.repositories.CategoryRepo;
import com.project.app.repositories.CommentRepo;
import com.project.app.repositories.FollowRepo;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.repositories.RoleRepo;
import com.project.app.repositories.TagRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private TagRepo tagRepo;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private BookmarkRepo bookmarkRepo;

    @Autowired
    private FollowRepo followRepo;

    @Override
    public UserDto promoteToAdmin(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));
        Role adminRole = this.adminRole();

        if (user.getRoles().stream().anyMatch(role -> role.getId().equals(adminRole.getId()))) {
            throw new ApiException("user is already an admin", HttpStatus.CONFLICT);
        }
        user.getRoles().add(adminRole);
        return this.modelMapper.map(this.userRepo.save(user), UserDto.class);
    }

    @Override
    public UserDto demoteFromAdmin(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(() -> new ResouceNotFoundException("User", "Id", userId));

        // an admin removing their own role could lock everybody out of the admin endpoints
        if (this.authUtil.getLoggedInUser().getId() == user.getId()) {
            throw new ApiException("you can not remove your own admin role");
        }

        boolean removed = user.getRoles().removeIf(role -> AppConstants.ADMIN_USER.equals(role.getId()));
        if (!removed) {
            throw new ApiException("user is not an admin");
        }
        return this.modelMapper.map(this.userRepo.save(user), UserDto.class);
    }

    @Override
    public StatsDto getStats() {
        return new StatsDto(
                this.userRepo.count(),
                this.postRepo.count(),
                this.commentRepo.count(),
                this.categoryRepo.count(),
                this.tagRepo.count(),
                this.postLikeRepo.count(),
                this.bookmarkRepo.count(),
                this.followRepo.count());
    }

    private Role adminRole() {
        return this.roleRepo.findById(AppConstants.ADMIN_USER)
                .orElseThrow(() -> new ResouceNotFoundException("Role", "Id", AppConstants.ADMIN_USER));
    }

}
