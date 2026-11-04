package com.project.app.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Notification;
import com.project.app.entities.User;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    Page<Notification> findByRecipient(User recipient, Pageable pageable);

    long countByRecipientAndReadFalse(User recipient);

    void deleteByRecipientOrActor(User recipient, User actor);

}
