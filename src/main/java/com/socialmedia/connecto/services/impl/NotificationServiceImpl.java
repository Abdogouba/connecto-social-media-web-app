package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.NotificationDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.Notification;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.NotificationRepository;
import com.socialmedia.connecto.services.NotificationService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    public PagedDTO<NotificationDTO> getMyNotifications(int page, int size) {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notificationPage = notificationRepository
                .findAllByReceiverOrderByCreatedAtDesc(user, pageable);

        List<NotificationDTO> dtos = notificationPage.getContent().stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(n.getId());
            dto.setSenderId(n.getSender().getId());
            dto.setSenderName(n.getSender().getName());
            dto.setType(n.getType().name());
            dto.setReferenceId(n.getReferenceId());
            dto.setRead(n.isRead());
            dto.setCreatedAt(n.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<NotificationDTO>(
                dtos,
                notificationPage.getNumber(),
                notificationPage.getTotalPages(),
                notificationPage.getTotalElements()
        );
    }

    @Override
    public long countUnreadNotifications() {
        User user = userService.getCurrentUser();
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }

    @Override
    public void markMyNotificationsAsRead() {
        User  user = userService.getCurrentUser();
        notificationRepository.markMyNotificationsAsRead(user);
    }

}
