package com.sporty.identity.services.impl;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoResponses.JwtAuthenticationResponse;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.dto.dtoResponses.SignInResponse;
import com.sporty.identity.dto.dtoResponses.SignUpResponse;
import com.sporty.identity.entities.Role;
import com.sporty.identity.entities.User;
import com.sporty.identity.repository.UserRepository;
import com.sporty.identity.services.AuthenticationService;
import com.sporty.identity.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;


    public SignUpResponse signup(SignUpRequest signUpRequest) {
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFirstname(signUpRequest.getFirstname());
        user.setLastname(signUpRequest.getLastname());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        userRepository.save(user);
        SignUpResponse signUpResponse = new SignUpResponse();
        signUpResponse.setFirstname(signUpRequest.getFirstname());
        signUpResponse.setLastname(signUpRequest.getLastname());
        signUpResponse.setEmail(signUpRequest.getEmail());
        return signUpResponse;
    }

    public SignInResponse signin (SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                (signInRequest.getEmail(), signInRequest.getPassword()));

        var user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));
        var jwt = jwtService.generateToken(user);
       // var refreshToken = jwtService.generateRefreshToken(new HashMap<>(),user);

        SignInResponse jwtAuthenticationResponse = new SignInResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setEmail(user.getEmail());

        return jwtAuthenticationResponse;

    }

    public JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
        String userEmail =jwtService.extractUserName(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(),user)){
            var jwt = jwtService.generateToken(user);
            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setRefreshToken(jwt);

            return jwtAuthenticationResponse;

        }
        return null;
    }
}
