package com.sporty.identity.dto.dtoResponses;

import lombok.Data;

@Data
public class UpdateProfileResponse {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
}
