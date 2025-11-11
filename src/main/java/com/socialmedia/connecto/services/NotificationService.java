package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.PagedNotificationDTO;
import org.springframework.stereotype.Service;

public interface NotificationService {

    PagedNotificationDTO getMyNotifications(int page, int size);


}
