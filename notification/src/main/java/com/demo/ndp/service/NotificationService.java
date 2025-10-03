package com.demo.ndp.service;

import com.demo.ndp.model.Notification;
import com.demo.ndp.repository.NotificationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class NotificationService {

    private NotificationRepository repository;
    @Getter
    private final BlockingQueue<Notification> queue = new LinkedBlockingQueue<>();

    public NotificationService(NotificationRepository notificationRepository){
        this.repository = notificationRepository;
    }

    public NotificationService() {

    }

    public Notification enqueue(Notification notification){
        repository.save(notification);
        queue.add(notification);
        return notification;
    }

    public Notification getStatus(String id){
        return repository.findById(id).orElse(null);
    }


}
