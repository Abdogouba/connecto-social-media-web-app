package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowSuggestionUserDTO {
    private Long id;
    private String name;
    private boolean isPrivate;
}

