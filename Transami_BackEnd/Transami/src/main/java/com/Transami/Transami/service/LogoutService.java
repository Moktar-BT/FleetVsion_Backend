package com.Transami.Transami.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LogoutService implements LogoutHandler {

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        // Set response status and message
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write("{\"message\": \"Logged out successfully\"}");
            response.getWriter().flush();
        } catch (IOException e) {
            // Log error if needed
        }
    }
}