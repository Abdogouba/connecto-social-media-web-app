package com.socialmedia.connecto.dtos;

import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Name should be 30 characters max")
    private String name;

    @Size(max = 50, message = "Location should be 50 characters max")
    private String location;

    @Size(max = 300, message = "Bio should be 300 characters max")
    private String bio;

    @NotNull(message = "IsPrivate is required")
    private Boolean isPrivate;

    private String pictureURL;

}
