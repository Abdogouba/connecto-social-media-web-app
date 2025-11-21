package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.LoginRequestDTO;
import com.socialmedia.connecto.dtos.LoginResponseDTO;
import com.socialmedia.connecto.dtos.UserRegistrationDTO;
import com.socialmedia.connecto.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO dto) {
        this.authenticationService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequestDTO dto) throws Exception {
        LoginResponseDTO response = authenticationService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok("Temporary password sent to your email, if email is valid.");
    }


}
