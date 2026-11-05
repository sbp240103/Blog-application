package com.project.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JwtAuthResponse {

    private String token;

    // use it on /api/v1/auth/refresh to get a new token when this one expires
    private String refreshToken;

    private UserDto user;

}
