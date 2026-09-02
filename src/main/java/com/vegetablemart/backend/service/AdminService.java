package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.user.UserResponse;
import com.vegetablemart.backend.entity.Role;

import java.util.List;

public interface AdminService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUserStatus(Long id, Boolean active);

    UserResponse updateUserRole(Long id, Role role);

    void deleteUser(Long id);
}

