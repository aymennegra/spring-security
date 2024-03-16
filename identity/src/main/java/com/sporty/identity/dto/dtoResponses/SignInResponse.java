package com.sporty.identity.dto.dtoResponses;

import lombok.Data;

@Data
public class SignInResponse {
    private String token;
    private String email;
}
