package com.demo.ndp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Notification {

    @Id
    private String id;

    private String userId;
    private String country;
    private String channel;
    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime scheduleAt;

    public Notification(){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.QUEUED;
    }
}
