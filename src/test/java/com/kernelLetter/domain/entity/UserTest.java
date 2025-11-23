package com.kernelLetter.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Test
    @DisplayName("User 엔티티를 빌더로 생성한다")
    void builder_Success() {
        // given
        Long id = 1L;
        String email = "test@example.com";
        String name = "Test User";

        // when
        User user = User.builder()
                .id(id)
                .email(email)
                .name(name)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("User를 필수 필드만으로 생성한다")
    void builder_MinimalFields_Success() {
        // when
        User user = User.builder()
                .email("minimal@example.com")
                .name("Minimal User")
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo("minimal@example.com");
        assertThat(user.getName()).isEqualTo("Minimal User");
    }

    @Test
    @DisplayName("User를 빈 필드값으로 생성한다")
    void builder_EmptyFields_Success() {
        // when
        User user = User.builder()
                .email("")
                .name("")
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEmpty();
        assertThat(user.getName()).isEmpty();
    }

    @Test
    @DisplayName("User를 null 필드값으로 생성한다")
    void builder_NullFields_Success() {
        // when
        User user = User.builder()
                .email(null)
                .name(null)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getName()).isNull();
    }

    @Test
    @DisplayName("긴 이메일 주소로 User를 생성한다")
    void builder_LongEmail_Success() {
        // given
        String longEmail = "very.long.email.address.for.testing.purposes@example.com";

        // when
        User user = User.builder()
                .email(longEmail)
                .name("Test User")
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("특수 문자가 포함된 이름으로 User를 생성한다")
    void builder_SpecialCharactersInName_Success() {
        // given
        String specialName = "홍길동-O'Brien";

        // when
        User user = User.builder()
                .email("test@example.com")
                .name(specialName)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("다양한 형식의 이메일로 User를 생성한다")
    void builder_VariousEmailFormats_Success() {
        // given & when & then
        User user1 = User.builder()
                .email("simple@example.com")
                .name("User 1")
                .build();
        assertThat(user1.getEmail()).isEqualTo("simple@example.com");

        User user2 = User.builder()
                .email("user+tag@example.co.kr")
                .name("User 2")
                .build();
        assertThat(user2.getEmail()).isEqualTo("user+tag@example.co.kr");

        User user3 = User.builder()
                .email("user.name@sub.example.com")
                .name("User 3")
                .build();
        assertThat(user3.getEmail()).isEqualTo("user.name@sub.example.com");
    }

    @Test
    @DisplayName("NoArgsConstructor로 User를 생성한다")
    void noArgsConstructor_Success() {
        // when
        User user = new User();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getName()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor로 User를 생성한다")
    void allArgsConstructor_Success() {
        // when
        User user = new User(1L, "all@example.com", "All Args User");

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("all@example.com");
        assertThat(user.getName()).isEqualTo("All Args User");
    }

    @Test
    @DisplayName("Getter 메서드가 올바르게 동작한다")
    void getters_Success() {
        // given
        User user = User.builder()
                .id(100L)
                .email("getter@example.com")
                .name("Getter Test")
                .build();

        // when & then
        assertThat(user.getId()).isEqualTo(100L);
        assertThat(user.getEmail()).isEqualTo("getter@example.com");
        assertThat(user.getName()).isEqualTo("Getter Test");
    }

    @Test
    @DisplayName("한글 이름으로 User를 생성한다")
    void builder_KoreanName_Success() {
        // when
        User user = User.builder()
                .email("korean@example.com")
                .name("김철수")
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("매우 긴 이름으로 User를 생성한다")
    void builder_VeryLongName_Success() {
        // given
        String longName = "a".repeat(255);

        // when
        User user = User.builder()
                .email("longname@example.com")
                .name(longName)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).hasSize(255);
    }
}