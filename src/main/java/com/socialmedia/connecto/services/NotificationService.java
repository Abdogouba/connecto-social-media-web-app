package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.NotificationDTO;
import com.socialmedia.connecto.dtos.PagedDTO;

public interface NotificationService {

    PagedDTO<NotificationDTO> getMyNotifications(int page, int size);

    long countUnreadNotifications();

    void markMyNotificationsAsRead();

}
