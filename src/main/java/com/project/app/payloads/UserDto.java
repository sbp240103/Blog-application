package com.project.app.payloads;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.project.app.validation.StrongPassword;

@NoArgsConstructor 
@Getter 
@Setter 

public class UserDto {
    private int id;

    @NotEmpty(message = "name should not be empty")
    @Size(min = 4, max = 20, message = "name must be between 4 and 20 characters")
    private String name;

    @NotNull(message = "email should not be null")
    @Email(message = "email should be a valid email address")
    private String email;

    @NotEmpty(message = "password should not be empty")
    @Size(min = 6, max = 15, message = "password must be between 6 and 15 characters")
    @StrongPassword
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotEmpty(message = "about should not be empty")
    private String about;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String avatarImage;

    // roles are managed by admins, never accepted from the request body
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<RoleDto> roles = new HashSet<>();
}
