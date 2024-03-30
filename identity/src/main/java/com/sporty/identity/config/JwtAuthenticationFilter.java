package com.sporty.identity.config;

import com.sporty.identity.services.JWTService;
import com.sporty.identity.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(javax.servlet.http.HttpServletRequest httpServletRequest, javax.servlet.http.HttpServletResponse httpServletResponse, javax.servlet.FilterChain filterChain) throws javax.servlet.ServletException, IOException {
        // Extract request path
        String requestURI = httpServletRequest.getRequestURI();

        // Check if the request path is signin or signup
        if (requestURI.equals("/api/v1/auth/signin") || requestURI.equals("/api/v1/auth/signup") || requestURI.equals("/api/v1/auth/refresh")|| requestURI.equals("/api/v1/auth/fb")) {
            // If it's signin or signup, proceed without authentication
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        // For other paths, continue with authentication
        final String authHeader = httpServletRequest.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserName(jwt);
        if (!org.apache.commons.lang3.StringUtils.isEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
