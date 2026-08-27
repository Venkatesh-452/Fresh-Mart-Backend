package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.auth.LoginRequest;
import com.vegetablemart.backend.dto.auth.LoginResponse;
import com.vegetablemart.backend.dto.auth.RegisterRequest;
import com.vegetablemart.backend.dto.user.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}