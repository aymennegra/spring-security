package com.sporty.identity.controller;


import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.dto.dtoResponses.ResponseHandler;
import com.sporty.identity.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignUpRequest signUpRequest) {
        try {
            return ResponseHandler.responseBuilder("user created", HttpStatus.OK,
                    authenticationService.signup(signUpRequest));
        } catch (Exception e) {
            // If an exception occurs, return unauthorized response
            return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<Object> signin(@RequestBody SignInRequest signInRequest) {
        try {
            return ResponseHandler.responseBuilder("Connected successfully", HttpStatus.OK,
                    authenticationService.signin(signInRequest));
        } catch (Exception e) {
            // If an exception occurs, return unauthorized response
            return ResponseHandler.responseBuilder("Invalid Email or Password", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            return ResponseHandler.responseBuilder("Token refreshed successfully", HttpStatus.OK,
                    authenticationService.refreshToken(refreshTokenRequest));
        } catch (Exception e) {
            // If an exception occurs, return unauthorized response
            return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }
}
