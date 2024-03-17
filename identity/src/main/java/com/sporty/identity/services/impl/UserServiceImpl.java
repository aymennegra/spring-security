package com.sporty.identity.services.impl;

import com.sporty.identity.dto.dtoRequests.UserProfileRequest;
import com.sporty.identity.dto.dtoResponses.UserProfileResponse;
import com.sporty.identity.entities.User;
import com.sporty.identity.repository.UserRepository;
import com.sporty.identity.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
            }
        };
    }

    public UserProfileResponse getUserProfile() {
        // Retrieve the currently authenticated user's details from the security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Check if the user exists in the database based on the email (assuming email is the username)
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Construct UserProfileResponse from user details
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(String.valueOf(user.getId()));
        userProfileResponse.setFirstname(user.getFirstname());
        userProfileResponse.setLastname(user.getLastname());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setPhone(user.getPhone());
        // Add other profile information as needed
        return userProfileResponse;
    }

    public UserProfileResponse updateUserProfile(UserProfileRequest userProfileRequest) {
        // Retrieve the currently authenticated user's details from the security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Check if the user exists in the database based on the email (assuming email is the username)
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update user information with the data from the userProfileRequest
        user.setFirstname(userProfileRequest.getFirstname());
        user.setLastname(userProfileRequest.getLastname());
        user.setEmail(userProfileRequest.getEmail());
        user.setPhone(userProfileRequest.getPhone());
        user.setPassword(userProfileRequest.getPassword());
        user.setPassword(new BCryptPasswordEncoder().encode(userProfileRequest.getPassword()));
        // Update other user information as needed

        // Save the updated user entity
        userRepository.save(user);

        // Construct and return a UserProfileResponse with updated user information
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstname(user.getFirstname());
        userProfileResponse.setLastname(user.getLastname());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setPhone(user.getPhone());
        // Add other profile information as needed
        return userProfileResponse;
    }

}


