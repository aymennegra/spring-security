package com.sporty.identity.services;

import com.sporty.identity.dto.dtoRequests.UserProfileRequest;
import com.sporty.identity.dto.dtoResponses.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    UserDetailsService userDetailsService();
    ResponseEntity<Object> getUserProfile ();
    ResponseEntity<Object> updateUserProfile(UserProfileRequest userProfileRequest);
}
