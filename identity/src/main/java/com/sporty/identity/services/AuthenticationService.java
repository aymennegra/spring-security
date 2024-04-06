package com.sporty.identity.services;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<Object> signup (SignUpRequest signUpRequest);
    ResponseEntity<Object> signin (SignInRequest signInRequest);
    ResponseEntity<Object> refreshToken (RefreshTokenRequest refreshTokenRequest);
}
