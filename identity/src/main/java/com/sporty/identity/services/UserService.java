package com.sporty.identity.services;

import com.sporty.identity.dto.dtoResponses.UserProfileResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    UserDetailsService userDetailsService();
    UserProfileResponse getUserProfile ();
}
