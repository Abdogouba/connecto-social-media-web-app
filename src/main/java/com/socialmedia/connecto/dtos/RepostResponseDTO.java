package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepostResponseDTO {
    private Long id;
    private Long reposterId;
    private String reposterName;
    private Long postId;
    private LocalDateTime createdAt;
}
