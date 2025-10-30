package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.ChangePasswordDTO;
import com.socialmedia.connecto.dtos.EditProfileDTO;
import com.socialmedia.connecto.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("User area");
    }

    @PutMapping("/edit-profile")
    public ResponseEntity<String> editProfile(@Valid @RequestBody EditProfileDTO dto) {
        try {
            userService.editProfile(dto);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        try {
            userService.changePassword(dto);
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
