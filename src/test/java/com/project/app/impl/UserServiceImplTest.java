package com.project.app.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.app.config.AppConstants;
import com.project.app.entities.Role;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.ChangePasswordRequest;
import com.project.app.payloads.UserDto;
import com.project.app.repositories.RoleRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.security.AuthUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUtil authUtil;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto newUserDto() {
        UserDto dto = new UserDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("secret123");
        dto.setAbout("about me");
        return dto;
    }

    private User user(int id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setPassword("stored-hash");
        return user;
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(this.userRepo.existsByEmail("test@example.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> this.userService.createUser(newUserDto()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(this.userRepo, never()).save(any());
    }

    @Test
    void createUserHashesPasswordAndAssignsNormalRole() {
        Role normalRole = new Role(AppConstants.NORMAL_USER, AppConstants.ROLE_NORMAL);
        when(this.userRepo.existsByEmail("test@example.com")).thenReturn(false);
        when(this.passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(this.roleRepo.findById(AppConstants.NORMAL_USER)).thenReturn(Optional.of(normalRole));
        when(this.userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        this.userService.createUser(newUserDto());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(this.userRepo).save(saved.capture());
        assertEquals("hashed-secret", saved.getValue().getPassword());
        assertTrue(saved.getValue().getRoles().contains(normalRole));
    }

    @Test
    void getUserByIdThrowsWhenUserIsMissing() {
        when(this.userRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResouceNotFoundException.class, () -> this.userService.getUserById(99));
    }

    @Test
    void updateUserIsNotAllowedForSomebodyElse() {
        when(this.userRepo.findById(2)).thenReturn(Optional.of(user(2, "two@example.com")));
        when(this.authUtil.getLoggedInUser()).thenReturn(user(1, "one@example.com"));
        when(this.authUtil.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> this.userService.updateUser(newUserDto(), 2));
        verify(this.userRepo, never()).save(any());
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User me = user(1, "one@example.com");
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.passwordEncoder.matches("wrong-old", "stored-hash")).thenReturn(false);

        assertThrows(ApiException.class,
                () -> this.userService.changePassword(new ChangePasswordRequest("wrong-old", "newpass1")));
        verify(this.userRepo, never()).save(any());
    }

    @Test
    void changePasswordStoresNewHash() {
        User me = user(1, "one@example.com");
        when(this.authUtil.getLoggedInUser()).thenReturn(me);
        when(this.passwordEncoder.matches("oldpass1", "stored-hash")).thenReturn(true);
        when(this.passwordEncoder.encode("newpass1")).thenReturn("new-hash");

        this.userService.changePassword(new ChangePasswordRequest("oldpass1", "newpass1"));

        assertEquals("new-hash", me.getPassword());
        verify(this.userRepo).save(me);
    }

}
