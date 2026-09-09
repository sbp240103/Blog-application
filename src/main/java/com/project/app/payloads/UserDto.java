package com.project.app.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String password;

    @NotEmpty(message = "about should not be empty")
    private String about;
}
