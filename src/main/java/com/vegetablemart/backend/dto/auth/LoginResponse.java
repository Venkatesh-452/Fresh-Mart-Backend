package com.vegetablemart.backend.dto.auth;

import com.vegetablemart.backend.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;

    private Long userId;

    private String name;

    private String email;

    private Role role;
}