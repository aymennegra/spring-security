package com.sporty.identity.services.impl;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * class to generate SigninKey
 */
public class KeyGenerator {

    public static void main(String[] args) {
        // Generate a random 256-bit (32 bytes) key
        byte[] keyBytes = generateRandomBytes(32);

        // Encode the key bytes in base64
        String base64EncodedKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Base64 Encoded Key: " + base64EncodedKey);
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
