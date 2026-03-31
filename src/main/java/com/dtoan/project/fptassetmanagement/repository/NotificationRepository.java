package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByUserIdAndNotificationKeyAndIsArchivedFalse(Long userId, String notificationKey);

    Optional<Notification> findByUserIdAndNotificationKeyAndIsArchivedTrue(Long userId, String notificationKey);

    List<Notification> findTop10ByUserIdAndIsArchivedFalseOrderByIsReadAscUpdatedAtDescCreatedAtDesc(Long userId);

    List<Notification> findTop10ByUserIdAndIsReadFalseAndIsArchivedFalseOrderByUpdatedAtDescCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsArchivedFalse(Long userId);

    long countByUserIdAndIsReadFalseAndIsArchivedFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
