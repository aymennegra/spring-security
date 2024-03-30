package com.sporty.identity.services;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface AuthenticationService {

    ResponseEntity<Object> signup (SignUpRequest signUpRequest);
    ResponseEntity<Object> signin (SignInRequest signInRequest);
    ResponseEntity<Object> signinWithFacebook (Principal principal);
    ResponseEntity<Object> refreshToken (RefreshTokenRequest refreshTokenRequest);
}
