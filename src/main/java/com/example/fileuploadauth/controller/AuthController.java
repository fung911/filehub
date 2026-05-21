package com.example.fileuploadauth.controller;

import com.example.fileuploadauth.dto.AuthRequest;
import com.example.fileuploadauth.dto.AuthResponse;
import com.example.fileuploadauth.dto.MessageResponse;
import com.example.fileuploadauth.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody AuthRequest request) {
        authService.register(request);
        return new MessageResponse("Registered successfully");
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MessageResponse me(Principal principal) {
        return new MessageResponse("Hello, " + principal.getName());
    }
}
