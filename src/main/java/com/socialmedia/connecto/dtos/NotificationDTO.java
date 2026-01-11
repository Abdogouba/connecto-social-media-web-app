package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String type;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}


