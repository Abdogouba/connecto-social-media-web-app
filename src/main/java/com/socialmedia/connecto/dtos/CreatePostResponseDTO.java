package com.socialmedia.connecto.dtos;

import com.socialmedia.connecto.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostResponseDTO {

    private Long id;

    private String content;

    private Long userId;

    private String userName;

    private LocalDateTime createdAt;

}

