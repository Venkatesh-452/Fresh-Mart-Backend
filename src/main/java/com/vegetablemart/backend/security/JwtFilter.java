package com.vegetablemart.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;


// =========================================================
// SKIP JWT FILTER FOR PUBLIC AUTH APIs
// =========================================================

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path = request.getServletPath();

        return path.equals("/api/users/register")
                || path.equals("/api/users/login");
    }


// =========================================================
// JWT FILTER
// =========================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");


        // No Authorization header
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // Extract JWT token
        String token =
                authHeader.substring(7);


        try {

            // Extract email from JWT
            String email =
                    jwtService.extractEmail(token);


            // Authenticate only if user is not already authenticated
            if (email != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {


                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);


                // Validate JWT token
                if (jwtService.isTokenValid(
                        token,
                        userDetails.getUsername()
                )) {


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(

                                    userDetails,

                                    null,

                                    userDetails.getAuthorities()
                            );


                    authentication.setDetails(

                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );


                    // Store authentication in Spring Security Context
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }

            }

        } catch (Exception exception) {

            // Invalid or expired JWT
            SecurityContextHolder.clearContext();
        }


        // Continue request
        filterChain.doFilter(request, response);
    }


}
