package com.example.fileuploadauth.dto;

public record AuthResponse(String token, String tokenType) {

    public static AuthResponse bearer(String token) {
        return new AuthResponse(token, "Bearer");
    }
}
