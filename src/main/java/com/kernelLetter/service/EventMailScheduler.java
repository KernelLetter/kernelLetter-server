package com.kernelLetter.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventMailScheduler {

    private final TaskScheduler taskScheduler;
    private final EventMailService eventMailService;

    @Value("${event.mail.send-at}")
    private String sendAt;

    @PostConstruct
    public void scheduleEventMail() {
        try {
            ZonedDateTime target = ZonedDateTime.parse(sendAt)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

            if (now.isAfter(target)) {
                log.warn("Configured event mail time {} is in the past. Skipping scheduling.", target);
                return;
            }

            Duration duration = Duration.between(now, target);
            log.info("Scheduling event mail to run in {} hours (at {}).", duration.toHours(), target);

            taskScheduler.schedule(eventMailService::sendAnnouncementMails, Date.from(target.toInstant()));
        } catch (Exception e) {
            log.error("Failed to schedule event mail", e);
        }
    }
}
