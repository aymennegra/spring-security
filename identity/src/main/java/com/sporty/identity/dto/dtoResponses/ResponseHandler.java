package com.sporty.identity.dto.dtoResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> responseBuilder(
            String message, HttpStatus httpStatusCode, Object responseObject
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("code", httpStatusCode.value());
        response.put("data", responseObject);

        return new ResponseEntity<>(response, httpStatusCode);
    }
}