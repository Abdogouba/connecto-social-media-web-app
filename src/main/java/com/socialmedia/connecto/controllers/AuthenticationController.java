package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.UserRegistrationDTO;
import com.socialmedia.connecto.services.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationServiceImpl;

    public AuthenticationController(AuthenticationServiceImpl authenticationServiceImpl) {
        this.authenticationServiceImpl = authenticationServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody UserRegistrationDTO dto) {
        try {
            this.authenticationServiceImpl.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
