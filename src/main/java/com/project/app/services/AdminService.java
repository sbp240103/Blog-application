package com.project.app.services;

import com.project.app.payloads.UserDto;

public interface AdminService {

    UserDto promoteToAdmin(Integer userId);

    UserDto demoteFromAdmin(Integer userId);

}
