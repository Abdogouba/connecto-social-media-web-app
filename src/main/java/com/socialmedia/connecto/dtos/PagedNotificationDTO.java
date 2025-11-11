package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedNotificationDTO {
    private List<NotificationDTO> notifications;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
