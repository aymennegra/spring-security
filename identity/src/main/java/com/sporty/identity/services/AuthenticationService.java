package com.sporty.identity.services;

import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoResponses.JwtAuthenticationResponse;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.dto.dtoResponses.SignInResponse;
import com.sporty.identity.dto.dtoResponses.SignUpResponse;
import com.sporty.identity.entities.User;

public interface AuthenticationService {

    SignUpResponse signup (SignUpRequest signUpRequest);
    SignInResponse signin (SignInRequest signInRequest);

    JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest);
}
