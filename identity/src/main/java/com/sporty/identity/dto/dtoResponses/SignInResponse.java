package com.sporty.identity.dto.dtoResponses;

import lombok.Data;

import java.util.Date;

@Data
public class SignInResponse {
    private String accessToken;
    private String refreshToken;
    private Date tokenExpirationDate;
    private String email;
}
