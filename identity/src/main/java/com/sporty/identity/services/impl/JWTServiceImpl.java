package com.sporty.identity.services.impl;

import com.sporty.identity.services.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

    private static final long REFRESH_TOKEN_VALIDITY = 100L * 365 * 24 * 3600 * 1000; // 1 year (in milliseconds)
    private static final long ACESS_TOKEN_VALIDITY = 3600 * 1000; // 1 hour (in milliseconds)
    private static final String REFRESH_TOKEN_ID_KEY = "refreshTokenId";

    //valid for 1 day
    public String generateToken(UserDetails userDetails){
        return Jwts.builder().setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACESS_TOKEN_VALIDITY))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        extraClaims.put(REFRESH_TOKEN_ID_KEY, generateRefreshTokenId());
        // Mark the token as unused initially
        extraClaims.put("used", false);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    // Helper method to generate a unique refresh token id (you can implement your logic here)
    private String generateRefreshTokenId() {
        // Generate a random UUID and return it as a string
        return UUID.randomUUID().toString();
    }

    public String extractUserName(String token){
        return extractClaim(token,Claims::getSubject);
    }

    private <T> T extractClaim(String token , Function<Claims,T> claimsResolvers){
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }
    private Key getSigninKey (){
        byte [] key = Decoders.BASE64.decode("PFt6w66tztO5vqIdEoKmsHJho4tg1JkXmPuFg9BIZPI=");
        return Keys.hmacShaKeyFor(key);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token).getBody();
    }

    public Boolean isTokenValid (String token,UserDetails userDetails){
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Date extractExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public String extractRefreshTokenId(String refreshToken) {
        // Parse the JWT and extract its claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        // Retrieve the refresh token ID from the claims
        return (String) claims.get("refreshTokenId");
    }

    private boolean isTokenExpired (String token){
        try {
            return extractClaim(token,Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
