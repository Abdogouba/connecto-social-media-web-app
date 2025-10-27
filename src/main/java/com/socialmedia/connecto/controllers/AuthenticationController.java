package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.LoginRequestDTO;
import com.socialmedia.connecto.dtos.LoginResponseDTO;
import com.socialmedia.connecto.dtos.UserRegistrationDTO;
import com.socialmedia.connecto.services.AuthenticationServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = authenticationServiceImpl.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authenticationServiceImpl.forgotPassword(email);
        return ResponseEntity.ok("Temporary password sent to your email, if email is valid.");
    }


}
