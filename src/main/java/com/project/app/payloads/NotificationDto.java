package com.project.app.payloads;

import java.time.LocalDateTime;

import com.project.app.entities.NotificationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class NotificationDto {

    private Integer id;
    private NotificationType type;
    private String message;
    private Integer postId;
    private Integer actorId;
    private String actorName;
    private boolean read;
    private LocalDateTime createdAt;

}
