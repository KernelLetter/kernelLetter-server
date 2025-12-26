/*
package com.kernelLetter.service;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.repository.LetterRepository;
import com.kernelLetter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventMailService {

    private static final String SUBJECT = "💌 커널레터 편지가 도착했습니다 — 지금 확인해 주세요!";
    private static final String HEADER = """
            안녕하세요, 커널레터입니다.

            패스트캠퍼스 AI 백엔드 부트캠프 수료식을 기념해
            동기들의 마음을 담은 작은 이벤트를 준비했습니다.

            커널레터에 도착한 특별 편지를 지금 확인해 주세요.
            함께 했던 시간들이 더 오래 기억에 남을 거예요.

            커널레터 서버는 1월 중순까지만 운영되니 그 전에 편지함을 꼭 확인해 주세요.
            따뜻한 연말 보내시길 바랍니다.

            커널레터 바로가기: https://kernelletter.p-e.kr/

            🎁 기여자: 김지은, 김동균, 조현희, 조건희

            """;

    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public void sendAnnouncementMails() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (!StringUtils.hasText(user.getEmail())) {
                continue;
            }

            List<Letter> receivedLetters = letterRepository.findAllByReceiverId(user.getId());
            String body = buildBody(receivedLetters);

            try {
                emailService.sendMail(user.getEmail(), SUBJECT, body);
                log.info("Event mail sent to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send event mail to {}", user.getEmail(), e);
            }
        }
    }

    private String buildBody(List<Letter> letters) {
        StringBuilder body = new StringBuilder(HEADER);

        if (letters.isEmpty()) {
            body.append("받은 편지가 아직 없습니다.\n");
            return body.toString();
        }

        for (Letter letter : letters) {
            String senderName = letter.getSender() != null && StringUtils.hasText(letter.getSender().getName())
                    ? letter.getSender().getName()
                    : "익명";
            body.append(senderName)
                    .append(": ")
                    .append(letter.getContent())
                    .append("\n");
        }

        return body.toString();
    }
}
*/
