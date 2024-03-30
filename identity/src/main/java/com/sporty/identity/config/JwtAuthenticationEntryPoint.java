package com.sporty.identity.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(javax.servlet.http.HttpServletRequest httpServletRequest, javax.servlet.http.HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        // Set the response status code to 401 (Unauthorized)
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Set the response content type to application/json
        httpServletResponse.setContentType("application/json");
        // Write the response body with the desired message
        httpServletResponse.getWriter().write("{\"code\": 401, \"message\": \"Unauthorized\"}");
    }
}
