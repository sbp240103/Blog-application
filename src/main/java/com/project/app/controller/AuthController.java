package com.project.app.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.entities.User;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.payloads.JwtAuthRequest;
import com.project.app.payloads.JwtAuthResponse;
import com.project.app.payloads.UserDto;
import com.project.app.repositories.UserRepo;
import com.project.app.security.JwtTokenHelper;
import com.project.app.services.UserService;

import jakarta.validation.Valid;
import com.project.app.entities.RefreshToken;
import com.project.app.payloads.RefreshTokenRequest;
import com.project.app.services.RefreshTokenService;
import com.project.app.payloads.ApiResponse;
import com.project.app.services.LoginAttemptService;
import com.project.app.payloads.ForgotPasswordRequest;
import com.project.app.payloads.ResetPasswordRequest;
import com.project.app.services.PasswordResetService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private PasswordResetService passwordResetService;

    // login and hand back a token along with the logged in user
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> createToken(@Valid @RequestBody JwtAuthRequest request) {

        this.authenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.jwtTokenHelper.generateToken(userDetails);

        User user = this.userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResouceNotFoundException("User", "email", 0));

        String refreshToken = this.refreshTokenService.createRefreshToken(user).getToken();

        return new ResponseEntity<>(new JwtAuthResponse(token, refreshToken, this.modelMapper.map(user, UserDto.class)), HttpStatus.OK);
    }

    // trade a valid refresh token for a fresh access token
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = this.refreshTokenService.rotate(this.refreshTokenService.verify(request.getRefreshToken()));
        User user = refreshToken.getUser();

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(user.getEmail());
        String token = this.jwtTokenHelper.generateToken(userDetails);

        return ResponseEntity.ok(new JwtAuthResponse(token, refreshToken.getToken(), this.modelMapper.map(user, UserDto.class)));
    }

    // logout revokes the refresh token, the short lived access token simply expires
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        this.refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse("logged out successfully", true));
    }

    // always answers the same way so it does not reveal which emails are registered
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        this.passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(new ApiResponse("if the email is registered, a reset link has been sent", true));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        this.passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse("password has been reset, please login", true));
    }

    // register a new account, every new account gets the normal user role
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        UserDto registeredUser = this.userService.registerNewUser(userDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    private void authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        try {
            this.authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            this.loginAttemptService.loginFailed(email);
            throw new BadCredentialsException("invalid email or password");
        }
        this.loginAttemptService.loginSucceeded(email);
    }

}
