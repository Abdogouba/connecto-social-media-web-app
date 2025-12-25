package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReposterDTO {
    private Long id;
    private String name;
    private LocalDateTime repostedAt;
}

