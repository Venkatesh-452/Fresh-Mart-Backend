package com.vegetablemart.backend.security;

import com.vegetablemart.backend.entity.User;
import com.vegetablemart.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;


// =========================================================
// LOAD USER BY EMAIL
// =========================================================

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {


        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );


        return org.springframework.security.core.userdetails.User
                .builder()

                // Username used by Spring Security
                .username(user.getEmail())

                // Encrypted password from database
                .password(user.getPassword())

                // CUSTOMER → ROLE_CUSTOMER
                // ADMIN → ROLE_ADMIN
                .roles(user.getRole().name())

                // Disable account if active = false
                .disabled(!Boolean.TRUE.equals(user.getActive()))

                .build();
    }


}
