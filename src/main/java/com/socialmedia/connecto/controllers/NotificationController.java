package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.NotificationDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PagedDTO<NotificationDTO>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedDTO<NotificationDTO> response = notificationService.getMyNotifications(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> countUnreadNotifications() {
        long count = notificationService.countUnreadNotifications();
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<String> markMyNotificationsAsRead() {
        notificationService.markMyNotificationsAsRead();
        return ResponseEntity.ok("Marked all notifications of user as read");
    }

}

