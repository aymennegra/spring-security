package com.sporty.identity.dto.dtoResponses;


import lombok.Data;

@Data
public class UserProfileResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
}
