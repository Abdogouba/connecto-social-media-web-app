package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Notification;
import com.socialmedia.connecto.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    // Get all notifications for a specific receiver, ordered by newest first
    Page<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // Count unread notifications of a user
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver = :receiver AND n.isRead = false")
    void markMyNotificationsAsRead(@Param("receiver") User receiver);

}
