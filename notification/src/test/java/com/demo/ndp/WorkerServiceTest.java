package com.demo.ndp;

import com.demo.ndp.model.Decision;
import com.demo.ndp.model.DecisionType;
import com.demo.ndp.model.Notification;
import com.demo.ndp.model.NotificationStatus;
import com.demo.ndp.repository.NotificationRepository;
import com.demo.ndp.service.CEPService;
import com.demo.ndp.service.ChannelSender;
import com.demo.ndp.service.NotificationService;
import com.demo.ndp.service.WorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class WorkerServiceTest {

    private NotificationRepository repository;
    private NotificationService notificationService;
    private CEPService cepService;
    private ChannelSender channelSender;
    private WorkerService workerService;

    @BeforeEach
    void setup() {
        repository = mock(NotificationRepository.class);
        notificationService = new NotificationService();
        cepService = mock(CEPService.class);
        channelSender = mock(ChannelSender.class);

        workerService = new WorkerService(notificationService, repository, cepService, channelSender);
    }

    @Test
    void testRetryLogicFailsAfterMaxAttempts() throws Exception {
        Notification notif = new Notification();
        notif.setId("id123");
        notif.setUserId("123");
        notif.setChannel("EMAIL");
        notif.setMessage("Retry test");

        notificationService.getQueue().add(notif);

        when(cepService.evaluate(notif)).thenReturn(new Decision(DecisionType.ALLOW));
        doThrow(new RuntimeException("fail")).when(channelSender).send(notif);

        for (int i = 0; i < 4; i++) {
            workerService.processQueue();
        }

        verify(repository, atLeastOnce()).save(notif);
        assertThat(notif.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void testRulesDrop() {
        Notification notif = new Notification();
        notif.setId("id123");
        notif.setMessage("Drop test");

        notificationService.getQueue().add(notif);

        when(cepService.evaluate(notif)).thenReturn(new Decision(DecisionType.DROP));

        workerService.processQueue();

        assertThat(notif.getStatus()).isEqualTo(NotificationStatus.DROPPED);
    }
}
