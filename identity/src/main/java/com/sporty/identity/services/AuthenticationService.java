package com.sporty.identity.services;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoResponses.JwtAuthenticationResponse;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.entities.User;

public interface AuthenticationService {

    User signup (SignUpRequest signUpRequest);
    JwtAuthenticationResponse signin (SignInRequest signInRequest);

    JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest);
}
