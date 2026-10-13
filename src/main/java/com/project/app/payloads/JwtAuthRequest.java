package com.project.app.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtAuthRequest {

    @NotEmpty(message = "email should not be empty")
    @Email(message = "email should be a valid email address")
    private String email;

    @NotEmpty(message = "password should not be empty")
    private String password;

}
