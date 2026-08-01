package com.nova.service;

import com.nova.dto.UserLoginDTO;
import com.nova.dto.UserRegisterDTO;
import com.nova.dto.UserUpdateDTO;
import com.nova.dto.UserWebLoginDTO;
import com.nova.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO dto);

    User register(UserRegisterDTO dto);

    User webLogin(UserWebLoginDTO dto, String clientIp);

    User updateProfile(Long userId, UserUpdateDTO dto);
}
