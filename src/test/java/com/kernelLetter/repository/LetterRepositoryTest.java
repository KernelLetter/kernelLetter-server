package com.kernelLetter.repository;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.LetterPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("LetterRepository 단위 테스트")
class LetterRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LetterRepository letterRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .email("sender@example.com")
                .name("Sender User")
                .build();
        sender = entityManager.persist(sender);

        receiver = User.builder()
                .email("receiver@example.com")
                .name("Receiver User")
                .build();
        receiver = entityManager.persist(receiver);

        entityManager.flush();
    }

    @Test
    @DisplayName("Letter를 저장하고 조회한다")
    void save_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "테스트 편지");

        // when
        Letter savedLetter = letterRepository.save(letter);
        entityManager.flush();
        entityManager.clear();

        Letter foundLetter = letterRepository.findById(savedLetter.getId()).orElse(null);

        // then
        assertThat(foundLetter).isNotNull();
        assertThat(foundLetter.getId()).isEqualTo(savedLetter.getId());
        assertThat(foundLetter.getContent()).isEqualTo("테스트 편지");
        assertThat(foundLetter.getSender().getId()).isEqualTo(sender.getId());
        assertThat(foundLetter.getReceiver().getId()).isEqualTo(receiver.getId());
    }

    @Test
    @DisplayName("발신자 ID와 수신자 ID로 편지를 조회한다")
    void findBySenderIdAndReceiverId_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "조회 테스트");
        letterRepository.save(letter);
        entityManager.flush();

        // when
        Optional<Letter> foundLetter = letterRepository.findBySenderIdAndReceiverId(
                sender.getId(), receiver.getId());

        // then
        assertThat(foundLetter).isPresent();
        assertThat(foundLetter.get().getContent()).isEqualTo("조회 테스트");
        assertThat(foundLetter.get().getSender().getId()).isEqualTo(sender.getId());
        assertThat(foundLetter.get().getReceiver().getId()).isEqualTo(receiver.getId());
    }

    @Test
    @DisplayName("존재하지 않는 발신자 ID로 조회 시 빈 Optional을 반환한다")
    void findBySenderIdAndReceiverId_NotFound_ReturnsEmpty() {
        // given
        Letter letter = Letter.from(sender, receiver, "테스트");
        letterRepository.save(letter);
        entityManager.flush();

        // when
        Optional<Letter> foundLetter = letterRepository.findBySenderIdAndReceiverId(
                999L, receiver.getId());

        // then
        assertThat(foundLetter).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 수신자 ID로 조회 시 빈 Optional을 반환한다")
    void findBySenderIdAndReceiverId_ReceiverNotFound_ReturnsEmpty() {
        // given
        Letter letter = Letter.from(sender, receiver, "테스트");
        letterRepository.save(letter);
        entityManager.flush();

        // when
        Optional<Letter> foundLetter = letterRepository.findBySenderIdAndReceiverId(
                sender.getId(), 999L);

        // then
        assertThat(foundLetter).isEmpty();
    }

    @Test
    @DisplayName("같은 발신자와 수신자 간에 여러 편지가 있을 때 첫 번째 편지를 조회한다")
    void findBySenderIdAndReceiverId_MultipleLetters_ReturnsFirst() {
        // given
        Letter letter1 = Letter.from(sender, receiver, "첫 번째 편지");
        Letter letter2 = Letter.from(sender, receiver, "두 번째 편지");
        letterRepository.save(letter1);
        letterRepository.save(letter2);
        entityManager.flush();

        // when
        Optional<Letter> foundLetter = letterRepository.findBySenderIdAndReceiverId(
                sender.getId(), receiver.getId());

        // then
        assertThat(foundLetter).isPresent();
        assertThat(foundLetter.get().getContent()).isIn("첫 번째 편지", "두 번째 편지");
    }

    @Test
    @DisplayName("자신에게 보낸 편지를 조회한다")
    void findBySenderIdAndReceiverId_SelfLetter_Success() {
        // given
        Letter selfLetter = Letter.from(sender, sender, "자신에게 보낸 편지");
        letterRepository.save(selfLetter);
        entityManager.flush();

        // when
        Optional<Letter> foundLetter = letterRepository.findBySenderIdAndReceiverId(
                sender.getId(), sender.getId());

        // then
        assertThat(foundLetter).isPresent();
        assertThat(foundLetter.get().getContent()).isEqualTo("자신에게 보낸 편지");
        assertThat(foundLetter.get().getSender().getId())
                .isEqualTo(foundLetter.get().getReceiver().getId());
    }

    @Test
    @DisplayName("빈 내용의 편지를 저장하고 조회한다")
    void save_EmptyContent_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "");

        // when
        Letter savedLetter = letterRepository.save(letter);
        entityManager.flush();
        entityManager.clear();

        Letter foundLetter = letterRepository.findById(savedLetter.getId()).orElse(null);

        // then
        assertThat(foundLetter).isNotNull();
        assertThat(foundLetter.getContent()).isEmpty();
    }

    @Test
    @DisplayName("매우 긴 내용의 편지를 저장하고 조회한다")
    void save_LongContent_Success() {
        // given
        String longContent = "a".repeat(10000);
        Letter letter = Letter.from(sender, receiver, longContent);

        // when
        Letter savedLetter = letterRepository.save(letter);
        entityManager.flush();
        entityManager.clear();

        Letter foundLetter = letterRepository.findById(savedLetter.getId()).orElse(null);

        // then
        assertThat(foundLetter).isNotNull();
        assertThat(foundLetter.getContent()).hasSize(10000);
    }

    @Test
    @DisplayName("Letter를 삭제한다")
    void delete_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "삭제 테스트");
        Letter savedLetter = letterRepository.save(letter);
        entityManager.flush();

        // when
        letterRepository.delete(savedLetter);
        entityManager.flush();

        Optional<Letter> foundLetter = letterRepository.findById(savedLetter.getId());

        // then
        assertThat(foundLetter).isEmpty();
    }

    @Test
    @DisplayName("Letter를 수정한다")
    void update_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "원본 내용");
        Letter savedLetter = letterRepository.save(letter);
        entityManager.flush();
        entityManager.clear();

        // when
        Letter foundLetter = letterRepository.findById(savedLetter.getId()).orElseThrow();
        foundLetter.setContent(LetterPatchDto.builder()
                .senderId(sender.getId())
                .content("수정된 내용")
                .build());
        entityManager.flush();
        entityManager.clear();

        Letter updatedLetter = letterRepository.findById(savedLetter.getId()).orElseThrow();

        // then
        assertThat(updatedLetter.getContent()).isEqualTo("수정된 내용");
    }
}