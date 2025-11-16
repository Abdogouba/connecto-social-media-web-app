package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.PagedNotificationDTO;
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
    public ResponseEntity<PagedNotificationDTO> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedNotificationDTO response = notificationService.getMyNotifications(page, size);
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

