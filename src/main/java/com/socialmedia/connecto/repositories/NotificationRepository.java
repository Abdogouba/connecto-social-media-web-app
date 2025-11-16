package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Notification;
import com.socialmedia.connecto.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    // Get all notifications for a specific receiver, ordered by newest first
    Page<Notification> findAllByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    // Count unread notifications of a user
    long countByReceiverAndIsReadFalse(User receiver);

}
