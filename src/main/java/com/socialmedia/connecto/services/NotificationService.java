package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.NotificationDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.Notification;

public interface NotificationService {

    PagedDTO<NotificationDTO> getMyNotifications(int page, int size);

    long countUnreadNotifications();

    void markMyNotificationsAsRead();

    void saveNotification(Notification notification);

}
