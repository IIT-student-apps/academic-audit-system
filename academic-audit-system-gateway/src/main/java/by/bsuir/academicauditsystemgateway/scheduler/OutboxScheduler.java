package by.bsuir.academicauditsystemgateway.scheduler;

import by.bsuir.academicauditsystemgateway.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxEventService outboxEventService;

    @Scheduled(fixedDelay = 5000)
    public void runOutboxPublisher() {
        outboxEventService.sendUnsentEvents();
    }
}
