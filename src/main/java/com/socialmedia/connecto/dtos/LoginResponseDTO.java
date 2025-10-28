package com.socialmedia.connecto.dtos;

import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Long id;
    private String name;
    private String email;
    private Gender gender;
    private LocalDate birthDate;
    private String location;
    private String bio;
    private boolean isPrivate;
    private Role role;
    private String pictureURL;
    private LocalDateTime createdAt;
}

