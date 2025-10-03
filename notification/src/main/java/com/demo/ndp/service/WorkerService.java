package com.demo.ndp.service;

import com.demo.ndp.model.Decision;
import com.demo.ndp.model.Notification;
import com.demo.ndp.model.NotificationStatus;
import com.demo.ndp.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkerService {

    private static final int MAX_RETRIES = 3;
    private final NotificationService notificationService;
    private final NotificationRepository repository;
    private final CEPService cepService;
    private final ChannelSender channelSender;

    private final Map<String, Integer> retryCount = new ConcurrentHashMap<>();

    public WorkerService(NotificationService notificationService, NotificationRepository notificationRepository,
                         CEPService cepService, ChannelSender channelSender){
        this.notificationService = notificationService;
        this.repository = notificationRepository;
        this.cepService = cepService;
        this.channelSender = channelSender;
    }

    @Scheduled(fixedRate = 2000)
    public void processQueue(){
        BlockingQueue<Notification> queue = notificationService.getQueue();
        Notification notification;

        while ((notification = queue.poll()) != null) {
            if(notification.getScheduleAt() != null && notification.getScheduleAt().isAfter(LocalDateTime.now())){
                queue.add(notification);
                continue;
            }

            Decision decision = cepService.evaluate(notification);

            switch (decision.getType()) {
                case DROP:
                    notification.setStatus(NotificationStatus.DROPPED);
                    break;
                case DELAY:
                    notification.setStatus(NotificationStatus.DELAYED);
                    notification.setScheduleAt(decision.getDelayUntil());
                    queue.add(notification);
                    break;
                case ALLOW:
                    try {

                        channelSender.send(notification);
                        notification.setStatus(NotificationStatus.SENT);
                        repository.save(notification);
                        retryCount.remove(notification.getId());

                    }catch (Exception e){
                        int attempts = retryCount.getOrDefault(notification.getId(), 0);

                        if(attempts < MAX_RETRIES){
                            retryCount.put(notification.getId(), attempts + 1);
                            notification.setStatus(NotificationStatus.RETRYING);
                            repository.save(notification);
                            queue.add(notification);
                            System.out.println("Retrying notification " + notification.getId() + " attempt: " + (attempts + 1));
                        }else {
                            notification.setStatus(NotificationStatus.FAILED);
                            repository.save(notification);
                            retryCount.remove(notification.getId());
                            System.out.println("Notification sending failed: " + notification.getId());
                        }
                    }
                    break;
            }
            repository.save(notification);
        }

    }
}
