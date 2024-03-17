package com.sporty.identity.services.impl;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.dto.dtoResponses.JwtAuthenticationResponse;
import com.sporty.identity.dto.dtoResponses.ResponseHandler;
import com.sporty.identity.dto.dtoResponses.SignInResponse;
import com.sporty.identity.dto.dtoResponses.SignUpResponse;
import com.sporty.identity.entities.Role;
import com.sporty.identity.entities.User;
import com.sporty.identity.repository.UserRepository;
import com.sporty.identity.services.AuthenticationService;
import com.sporty.identity.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public ResponseEntity<Object> signup(SignUpRequest signUpRequest) {
        // Check if a user with the provided email already exists
        Optional<User> existingUserOptional = userRepository.findByEmail(signUpRequest.getEmail());
        // Check if a user with the provided phone number already exists
        Optional<User> existingUserByPhone = userRepository.findByPhone(signUpRequest.getPhone());
        if  (existingUserOptional.isPresent()){
            // User with the provided email already exists
            return ResponseHandler.responseBuilder("User with email " + signUpRequest.getEmail() + " already exists", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        } else if (existingUserByPhone.isPresent()) {
            // User with the provided phone already exists
            return ResponseHandler.responseBuilder("User with phone number " + signUpRequest.getPhone() + " already exists", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        } else {//ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
            // Create a new user
            try {
                User user = new User();
                user.setEmail(signUpRequest.getEmail());
                user.setFirstname(signUpRequest.getFirstname());
                user.setLastname(signUpRequest.getLastname());
                user.setPhone(signUpRequest.getPhone());
                user.setRole(Role.USER);
                user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
                userRepository.save(user);
                var jwt = jwtService.generateToken(user);
                SignUpResponse signUpResponse = new SignUpResponse();
                signUpResponse.setFirstname(signUpRequest.getFirstname());
                signUpResponse.setLastname(signUpRequest.getLastname());
                signUpResponse.setEmail(signUpRequest.getEmail());
                signUpResponse.setPhone(signUpRequest.getPhone());
                signUpResponse.setToken(jwt);
                return ResponseHandler.responseBuilder("user created", HttpStatus.OK,
                        signUpResponse);
            } catch (Exception e) {
                return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
            }
        }
    }

    public ResponseEntity<Object> signin(SignInRequest signInRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                    (signInRequest.getEmail(), signInRequest.getPassword()));

            var user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));
            var jwt = jwtService.generateToken(user);
            // var refreshToken = jwtService.generateRefreshToken(new HashMap<>(),user);
            SignInResponse jwtAuthenticationResponse = new SignInResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setEmail(user.getEmail());
            return ResponseHandler.responseBuilder("Connected successfully", HttpStatus.OK,
                    jwtAuthenticationResponse);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder("Invalid Email or Password", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }

    public ResponseEntity<Object> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
            var jwt = jwtService.generateToken(user);
            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setRefreshToken(jwt);
            return ResponseHandler.responseBuilder("Token refreshed successfully", HttpStatus.OK,
                    jwtAuthenticationResponse);
        }
        return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
    }
}
