package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Notification findNotificationById(Long id);

    @Query("SELECT n FROM Notification n where n.seen = 'false'")
    List<Notification> getAllUnseenNotifications();
}