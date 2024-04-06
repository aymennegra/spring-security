package com.sporty.identity.dto.dtoResponses;

import lombok.Data;

@Data
public class SignUpResponse  {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String accessToken;
    private String refreshToken;
}
