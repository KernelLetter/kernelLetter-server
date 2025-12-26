package com.kernelLetter.controller.mail;

import com.kernelLetter.service.EventMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이벤트 메일 발송 API 컨트롤러
 */
@RestController
@RequestMapping("/mail/event")
@RequiredArgsConstructor
@Slf4j
public class EventMailController {

    private final EventMailService eventMailService;

    /**
     * 모든 사용자에게 이벤트 메일 발송
     *
     * @return 발송 결과 메시지
     */
    /*@PostMapping("/send")*/
    public ResponseEntity<String> sendEventMail() {
        log.info("Event mail send requested");

        try {
            eventMailService.sendAnnouncementMails();
            return ResponseEntity.ok("이벤트 메일 발송이 완료되었습니다.");
        } catch (IllegalStateException e) {
            log.warn("Duplicate send attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send event mail", e);
            return ResponseEntity.internalServerError().body("메일 발송 중 오류가 발생했습니다.");
        }
    }
}
