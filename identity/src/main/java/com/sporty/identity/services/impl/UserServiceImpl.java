package com.sporty.identity.services.impl;

import com.sporty.identity.dto.dtoRequests.UserProfileRequest;
import com.sporty.identity.dto.dtoResponses.ResponseHandler;
import com.sporty.identity.dto.dtoResponses.UpdateProfileResponse;
import com.sporty.identity.dto.dtoResponses.UserProfileResponse;
import com.sporty.identity.entities.User;
import com.sporty.identity.repository.UserRepository;
import com.sporty.identity.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
    }

    public ResponseEntity<Object> getUserProfile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Check if a user with the provided email already exists
            try {
                // Retrieve the currently authenticated user's details from the security context
                // Check if the user exists in the database based on the email (assuming email is the username)
                User user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                // Construct UserProfileResponse from user details
                UserProfileResponse userProfileResponse = new UserProfileResponse();
                userProfileResponse.setId(String.valueOf(user.getUser_id()));
                userProfileResponse.setFirstname(user.getFirstname());
                userProfileResponse.setLastname(user.getLastname());
                userProfileResponse.setEmail(user.getEmail());
                userProfileResponse.setPhone(user.getPhone());
                // Add other profile information as needed
                return ResponseHandler.responseBuilder("User found", HttpStatus.OK,
                        userProfileResponse);
            }catch (Exception e){
                return ResponseHandler.responseBuilder("User not found", HttpStatus.UNAUTHORIZED,
                        new ArrayList<>());
            }
        }

    public ResponseEntity<Object> updateUserProfile(UserProfileRequest userProfileRequest) {
        try {// Retrieve the currently authenticated user's details from the security context
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Check if the user exists in the database based on the email (assuming email is the username)
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Update user information with the data from the userProfileRequest if not null
            if (userProfileRequest.getFirstname() != null) {
                user.setFirstname(userProfileRequest.getFirstname());
            }
            if (userProfileRequest.getLastname() != null) {
                user.setLastname(userProfileRequest.getLastname());
            }
            if (userProfileRequest.getEmail() != null) {
                user.setEmail(userProfileRequest.getEmail());
            }
            if (userProfileRequest.getPhone() != null) {
                user.setPhone(userProfileRequest.getPhone());
            }
            if (userProfileRequest.getPassword() != null) {
                // Update password only if not null
                user.setPassword(new BCryptPasswordEncoder().encode(userProfileRequest.getPassword()));
            }
            // Save the updated user entity
            userRepository.save(user);

            // Construct and return a UserProfileResponse with updated user information
            UpdateProfileResponse userProfileResponse = new UpdateProfileResponse();
            userProfileResponse.setFirstname(user.getFirstname());
            userProfileResponse.setLastname(user.getLastname());
            userProfileResponse.setEmail(user.getEmail());
            userProfileResponse.setPhone(user.getPhone());
            // Add other profile information as needed
            return ResponseHandler.responseBuilder("User Edited", HttpStatus.OK,
                    userProfileResponse);
        }
        catch (Exception e){
            return ResponseHandler.responseBuilder("An error has occurred", HttpStatus.UNAUTHORIZED,
                    new ArrayList<>());
        }
    }
}


