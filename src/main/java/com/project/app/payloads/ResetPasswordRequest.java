package com.project.app.payloads;

import com.project.app.validation.StrongPassword;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {

    @NotEmpty(message = "token should not be empty")
    private String token;

    @NotEmpty(message = "new password should not be empty")
    @Size(min = 6, max = 15, message = "new password must be between 6 and 15 characters")
    @StrongPassword
    private String newPassword;

}
