package com.project.app.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.project.app.validation.StrongPassword;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChangePasswordRequest {

    @NotEmpty(message = "old password should not be empty")
    private String oldPassword;

    @NotEmpty(message = "new password should not be empty")
    @Size(min = 6, max = 15, message = "new password must be between 6 and 15 characters")
    @StrongPassword
    private String newPassword;

}
