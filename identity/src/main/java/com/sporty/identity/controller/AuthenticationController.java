package com.sporty.identity.controller;


import com.sporty.identity.dto.dtoRequests.RefreshTokenRequest;
import com.sporty.identity.dto.dtoRequests.SignInRequest;
import com.sporty.identity.dto.dtoRequests.SignUpRequest;
import com.sporty.identity.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@EnableOAuth2Sso
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignUpRequest signUpRequest) {
        return authenticationService.signup(signUpRequest);
    }

    @PostMapping("/signin")
    public ResponseEntity<Object> signin(@RequestBody SignInRequest signInRequest) {
        return authenticationService.signin(signInRequest);
    }
    @GetMapping("/fb-auth")
    public ResponseEntity<Object> signinWithFacebook(Principal principal) {
        return authenticationService.signinWithFacebook(principal);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authenticationService.refreshToken(refreshTokenRequest);
    }
}
