package com.sporty.identity.services.impl;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.dto.dtoResponses.JwtAuthenticationResponse;
import com.sporty.identity.dto.dtoResponses.ResponseHandler;
import com.sporty.identity.dto.dtoResponses.SignInResponse;
import com.sporty.identity.dto.dtoResponses.SignUpResponse;
import com.sporty.identity.entities.RefreshToken;
import com.sporty.identity.entities.Role;
import com.sporty.identity.entities.User;
import com.sporty.identity.repository.RefreshTokenRepository;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 24; // 5 minutes (in milliseconds)

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
                signUpResponse.setAccessToken(jwt);
                return ResponseHandler.responseBuilder("user created", HttpStatus.OK,
                        signUpResponse);
            } catch (Exception e) {
                return ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
            }
        }
    }

    public ResponseEntity<Object> signin(SignInRequest signInRequest) {
        String refreshToken;
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                    (signInRequest.getEmail(), signInRequest.getPassword()));

            var user = userRepository.findByEmail(signInRequest.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));

            // Generate a refresh token if the user doesn't have one already
            if (user.getRefreshToken() == null) {
                refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
                user.setRefreshToken(refreshToken);
                userRepository.save(user);
                // Save the refresh token entity into the database
                RefreshToken refreshTokenEntity = new RefreshToken();
                refreshTokenEntity.setToken(refreshToken);
                refreshTokenEntity.setUser(user);
                refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
                refreshTokenEntity.setUsed(false); // Mark the token as unused initially
                refreshTokenRepository.save(refreshTokenEntity);
            }else {
                RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(user.getRefreshToken())
                        .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

                if (refreshTokenEntity.isUsed()) {
                    // Generate a new refresh token
                    refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
                    user.setRefreshToken(refreshToken);
                    refreshTokenEntity.setToken(refreshToken);
                    refreshTokenEntity.setUsed(false);
                    refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
                    refreshTokenRepository.save(refreshTokenEntity);
                    userRepository.save(user);
                } else {
                    // Use the existing refresh token
                    user.setRefreshToken(user.getRefreshToken());
                }
            }

            // Generate a new access token
            var jwt = jwtService.generateToken(user);

            // Extract expiration date of the access token
            Date expirationDate = jwtService.extractExpirationDate(jwt);

            // Prepare the response
            SignInResponse jwtAuthenticationResponse = new SignInResponse();
            jwtAuthenticationResponse.setAccessToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(user.getRefreshToken());
            jwtAuthenticationResponse.setTokenExpirationDate(expirationDate);
            jwtAuthenticationResponse.setEmail(user.getEmail());

            return ResponseHandler.responseBuilder("Connected successfully", HttpStatus.OK,
                    jwtAuthenticationResponse);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder("Invalid Email or Password", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }


    public ResponseEntity<Object> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            String userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenRequest.getToken())
                    .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

            if (refreshTokenEntity.isUsed()) {
                throw new IllegalArgumentException("Refresh token has already been used");
            }
            // Validate expiration date if needed
            refreshTokenEntity.setUsed(true); // Mark the token as used
            refreshTokenRepository.save(refreshTokenEntity);

            String newAccessToken = jwtService.generateToken(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setRefreshToken(newAccessToken);
            return ResponseHandler.responseBuilder("Token refreshed successfully", HttpStatus.OK, jwtAuthenticationResponse);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder("Invalid refresh token", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }
}
