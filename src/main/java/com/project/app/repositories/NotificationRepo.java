package com.project.app.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Notification;
import com.project.app.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    Page<Notification> findByRecipient(User recipient, Pageable pageable);

    long countByRecipientAndReadFalse(User recipient);

    @Modifying
    @Transactional
    @Query("update Notification n set n.read = true where n.recipient = :recipient and n.read = false")
    int markAllRead(@Param("recipient") User recipient);

    void deleteByRecipientOrActor(User recipient, User actor);

}
