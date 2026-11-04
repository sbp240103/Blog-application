package com.project.app.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// something that happened to a user, e.g. someone liked their post
@Entity
@Table(name = "notifications")
@NoArgsConstructor
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // who receives the notification
    @ManyToOne
    private User recipient;

    // who caused it
    @ManyToOne
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    // post the notification is about, empty for follows
    private Integer postId;

    @Column(length = 255)
    private String message;

    // read is a reserved word in mysql
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
