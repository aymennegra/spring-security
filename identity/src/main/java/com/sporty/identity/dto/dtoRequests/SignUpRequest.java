package com.sporty.identity.dto.dtoRequests;

import lombok.Data;

@Data
public class SignUpRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String password;
}
