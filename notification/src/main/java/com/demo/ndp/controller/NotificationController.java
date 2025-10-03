package com.demo.ndp.controller;

import com.demo.ndp.model.Notification;
import com.demo.ndp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    public NotificationController(NotificationService service){
        this.notificationService = service;
    }

    @PostMapping
    public Notification send(@RequestBody Notification notification){
        return notificationService.enqueue(notification);
    }

    @GetMapping("/{id}/status")
    public String status(@PathVariable String id){
        Notification notification = notificationService.getStatus(id);
        return notification != null ? notification.getStatus().toString() : "NOT_FOUND";
    }
}
