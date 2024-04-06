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
    private static final long REFRESH_TOKEN_VALIDITY = 100L * 365 * 24 * 3600 * 1000; // 5 minutes (in milliseconds)

    public ResponseEntity<Object> signup(SignUpRequest signUpRequest) {
        String refreshToken;
        String extractedRefreshtokenId;
        // Check if a user with the provided email already exists
        Optional<User> existingUserOptional = userRepository.findByEmail(signUpRequest.getEmail());
        // Check if a user with the provided phone number already exists
        Optional<User> existingUserByPhone = userRepository.findByPhone(signUpRequest.getPhone());
        if (existingUserOptional.isPresent()) {
            // User with the provided email already exists
            return ResponseHandler.responseBuilder("User with email " + signUpRequest.getEmail() + " already exists", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        } else if (existingUserByPhone.isPresent()) {
            // User with the provided phone already exists
            return ResponseHandler.responseBuilder("User with phone number " + signUpRequest.getPhone() + " already exists", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        } else {//ResponseHandler.responseBuilder("Unauthorized", HttpStatus.UNAUTHORIZED, new ArrayList<>());
            // Create a new user
            try {
                //save user info
                User user = new User();
                refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
                extractedRefreshtokenId = jwtService.extractRefreshTokenId(refreshToken);
                user.setEmail(signUpRequest.getEmail());
                user.setFirstname(signUpRequest.getFirstname());
                user.setLastname(signUpRequest.getLastname());
                user.setPhone(signUpRequest.getPhone());
                user.setRole(Role.USER);
                user.setRefreshToken(extractedRefreshtokenId);
                user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
                userRepository.save(user);
                //update refresh token table
                RefreshToken refreshTokenEntity = new RefreshToken();
                refreshTokenEntity.setToken(extractedRefreshtokenId);
                refreshTokenEntity.setUser(user);
                refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
                refreshTokenRepository.save(refreshTokenEntity);
                //get response
                var jwt = jwtService.generateToken(user);
                SignUpResponse signUpResponse = new SignUpResponse();
                signUpResponse.setFirstname(signUpRequest.getFirstname());
                signUpResponse.setLastname(signUpRequest.getLastname());
                signUpResponse.setEmail(signUpRequest.getEmail());
                signUpResponse.setPhone(signUpRequest.getPhone());
                signUpResponse.setRefreshToken(user.getRefreshToken());
                signUpResponse.setAccessToken(jwt);
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

            var user = userRepository.findByEmail(signInRequest.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));

            RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
            // Generate a new refresh token
            String newRefreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            // Generate a new access token
            var jwt = jwtService.generateToken(user);
            refreshTokenEntity.setToken(jwtService.extractRefreshTokenId(newRefreshToken));
            refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
            refreshTokenRepository.save(refreshTokenEntity);
            user.setRefreshToken(jwtService.extractRefreshTokenId(newRefreshToken));
            userRepository.save(user);

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
            User user = userRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
                    .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

            //generate new access token
            String newAccessToken = jwtService.generateToken(user);
            // Generate a new refresh token
            String newRefreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            refreshTokenEntity.setToken(jwtService.extractRefreshTokenId(newRefreshToken));
            refreshTokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
            refreshTokenRepository.save(refreshTokenEntity);

            user.setRefreshToken(jwtService.extractRefreshTokenId(newRefreshToken));
            userRepository.save(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setRefreshToken(user.getRefreshToken());
            jwtAuthenticationResponse.setAccessToken(newAccessToken);
            return ResponseHandler.responseBuilder("Token refreshed successfully", HttpStatus.OK, jwtAuthenticationResponse);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder("Invalid refresh token", HttpStatus.UNAUTHORIZED, new ArrayList<>());
        }
    }
}
