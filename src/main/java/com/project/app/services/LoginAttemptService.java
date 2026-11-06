package com.project.app.services;

public interface LoginAttemptService {

    void loginFailed(String email);

    void loginSucceeded(String email);

}
