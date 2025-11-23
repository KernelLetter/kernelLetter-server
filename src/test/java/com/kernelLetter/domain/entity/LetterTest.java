package com.kernelLetter.domain.entity;

import com.kernelLetter.dto.LetterPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Letter 엔티티 단위 테스트")
class LetterTest {

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .email("sender@example.com")
                .name("Sender User")
                .build();

        receiver = User.builder()
                .id(2L)
                .email("receiver@example.com")
                .name("Receiver User")
                .build();
    }

    @Test
    @DisplayName("Letter 엔티티를 빌더로 생성한다")
    void builder_Success() {
        // given
        String content = "테스트 편지 내용";

        // when
        Letter letter = Letter.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getId()).isEqualTo(1L);
        assertThat(letter.getSender()).isEqualTo(sender);
        assertThat(letter.getReceiver()).isEqualTo(receiver);
        assertThat(letter.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("from 팩토리 메서드로 Letter를 생성한다")
    void from_Success() {
        // given
        String content = "팩토리 메서드로 생성한 편지";

        // when
        Letter letter = Letter.from(sender, receiver, content);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getSender()).isEqualTo(sender);
        assertThat(letter.getReceiver()).isEqualTo(receiver);
        assertThat(letter.getContent()).isEqualTo(content);
        assertThat(letter.getId()).isNull();
    }

    @Test
    @DisplayName("빈 내용으로 Letter를 생성한다")
    void from_EmptyContent_Success() {
        // given
        String emptyContent = "";

        // when
        Letter letter = Letter.from(sender, receiver, emptyContent);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getContent()).isEmpty();
    }

    @Test
    @DisplayName("null 내용으로 Letter를 생성한다")
    void from_NullContent_Success() {
        // when
        Letter letter = Letter.from(sender, receiver, null);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getContent()).isNull();
    }

    @Test
    @DisplayName("매우 긴 내용으로 Letter를 생성한다")
    void from_LongContent_Success() {
        // given
        String longContent = "a".repeat(100000);

        // when
        Letter letter = Letter.from(sender, receiver, longContent);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getContent()).hasSize(100000);
    }

    @Test
    @DisplayName("같은 사용자가 발신자이자 수신자인 Letter를 생성한다")
    void from_SelfLetter_Success() {
        // given
        String content = "자신에게 보내는 편지";

        // when
        Letter letter = Letter.from(sender, sender, content);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getSender()).isEqualTo(letter.getReceiver());
    }

    @Test
    @DisplayName("특수 문자가 포함된 내용으로 Letter를 생성한다")
    void from_SpecialCharacters_Success() {
        // given
        String specialContent = "특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // when
        Letter letter = Letter.from(sender, receiver, specialContent);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getContent()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("이모지가 포함된 내용으로 Letter를 생성한다")
    void from_EmojiContent_Success() {
        // given
        String emojiContent = "안녕하세요! 😊👋 좋은 하루 되세요! 🌟";

        // when
        Letter letter = Letter.from(sender, receiver, emojiContent);

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getContent()).isEqualTo(emojiContent);
    }

    @Test
    @DisplayName("setContent 메서드로 편지 내용을 수정한다")
    void setContent_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "원본 내용");
        LetterPatchDto patchDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("수정된 내용")
                .build();

        // when
        letter.setContent(patchDto);

        // then
        assertThat(letter.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("setContent 메서드로 빈 내용으로 수정한다")
    void setContent_EmptyContent_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "원본 내용");
        LetterPatchDto patchDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("")
                .build();

        // when
        letter.setContent(patchDto);

        // then
        assertThat(letter.getContent()).isEmpty();
    }

    @Test
    @DisplayName("setContent 메서드로 null 내용으로 수정한다")
    void setContent_NullContent_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "원본 내용");
        LetterPatchDto patchDto = LetterPatchDto.builder()
                .senderId(1L)
                .content(null)
                .build();

        // when
        letter.setContent(patchDto);

        // then
        assertThat(letter.getContent()).isNull();
    }

    @Test
    @DisplayName("여러 번 setContent를 호출하여 내용을 변경한다")
    void setContent_MultipleTimes_Success() {
        // given
        Letter letter = Letter.from(sender, receiver, "원본 내용");

        // when
        letter.setContent(LetterPatchDto.builder().senderId(1L).content("첫 번째 수정").build());
        letter.setContent(LetterPatchDto.builder().senderId(1L).content("두 번째 수정").build());
        letter.setContent(LetterPatchDto.builder().senderId(1L).content("세 번째 수정").build());

        // then
        assertThat(letter.getContent()).isEqualTo("세 번째 수정");
    }

    @Test
    @DisplayName("NoArgsConstructor로 Letter를 생성한다")
    void noArgsConstructor_Success() {
        // when
        Letter letter = new Letter();

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getId()).isNull();
        assertThat(letter.getSender()).isNull();
        assertThat(letter.getReceiver()).isNull();
        assertThat(letter.getContent()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor로 Letter를 생성한다")
    void allArgsConstructor_Success() {
        // when
        Letter letter = new Letter(1L, sender, receiver, "모든 인자 생성자");

        // then
        assertThat(letter).isNotNull();
        assertThat(letter.getId()).isEqualTo(1L);
        assertThat(letter.getSender()).isEqualTo(sender);
        assertThat(letter.getReceiver()).isEqualTo(receiver);
        assertThat(letter.getContent()).isEqualTo("모든 인자 생성자");
    }
}