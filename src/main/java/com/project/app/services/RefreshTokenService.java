package com.project.app.services;

import com.project.app.entities.RefreshToken;
import com.project.app.entities.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verify(String token);

}
