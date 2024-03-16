package com.sporty.identity.controller;

import com.sporty.identity.dto.dtoResponses.ResponseHandler;
import com.sporty.identity.dto.dtoResponses.UserProfileResponse;
import com.sporty.identity.services.UserService;
import com.sporty.identity.services.impl.AuthenticationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<Object> getUserProfile() {
        try {
            return ResponseHandler.responseBuilder("user retrieved", HttpStatus.OK,
                    userService.getUserProfile());
        } catch (Exception e) {
            // If an exception occurs, return unauthorized response
            return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }
}
