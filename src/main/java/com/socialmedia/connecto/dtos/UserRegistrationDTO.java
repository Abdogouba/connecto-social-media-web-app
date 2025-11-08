package com.socialmedia.connecto.dtos;

import com.socialmedia.connecto.models.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Name should be 30 characters max")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is incorrect")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @NotBlank(message = "Gender is required")
    private String gender;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Size(max = 50, message = "Location should be 50 characters max")
    private String location;

    @Size(max = 300, message = "Bio should be 300 characters max")
    private String bio;

}


