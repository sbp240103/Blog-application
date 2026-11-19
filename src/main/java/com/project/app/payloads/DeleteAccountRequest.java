package com.project.app.payloads;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeleteAccountRequest {

    // asked again so a stolen token alone can not delete the account
    @NotEmpty(message = "password should not be empty")
    private String password;

}
