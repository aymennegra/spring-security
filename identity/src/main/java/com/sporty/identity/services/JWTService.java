package com.sporty.identity.services;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface JWTService {

    String extractUserName (String token);

    String generateToken (UserDetails userDetails);

    Boolean isTokenValid (String token,UserDetails userDetails);

    Date extractExpirationDate(String token);

    String extractRefreshTokenId(String refreshToken);

    String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails);
}
