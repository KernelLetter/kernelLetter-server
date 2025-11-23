package com.kernelLetter.repository;

import com.kernelLetter.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository 단위 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User를 저장하고 조회한다")
    void save_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("이름으로 User를 조회한다")
    void findByName_Success() {
        // given
        User user = User.builder()
                .email("findbyname@example.com")
                .name("FindByName User")
                .build();
        userRepository.save(user);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByName("FindByName User");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("FindByName User");
        assertThat(foundUser.get().getEmail()).isEqualTo("findbyname@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회 시 빈 Optional을 반환한다")
    void findByName_NotFound_ReturnsEmpty() {
        // when
        Optional<User> foundUser = userRepository.findByName("NonExistent User");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("ID로 User를 조회한다")
    void findById_Success() {
        // given
        User user = User.builder()
                .email("findbyid@example.com")
                .name("FindById User")
                .build();
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("FindById User");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
    void findById_NotFound_ReturnsEmpty() {
        // when
        Optional<User> foundUser = userRepository.findById(999L);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("같은 이름을 가진 여러 User가 있을 때 첫 번째 User를 조회한다")
    void findByName_MultipleUsers_ReturnsFirst() {
        // given
        User user1 = User.builder()
                .email("user1@example.com")
                .name("Duplicate Name")
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .name("Duplicate Name")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByName("Duplicate Name");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Duplicate Name");
        assertThat(foundUser.get().getEmail()).isIn("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("User를 삭제한다")
    void delete_Success() {
        // given
        User user = User.builder()
                .email("delete@example.com")
                .name("Delete User")
                .build();
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // when
        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("빈 이메일로 User를 저장한다")
    void save_EmptyEmail_Success() {
        // given
        User user = User.builder()
                .email("")
                .name("Empty Email User")
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("빈 이름으로 User를 저장한다")
    void save_EmptyName_Success() {
        // given
        User user = User.builder()
                .email("emptyname@example.com")
                .name("")
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByName("");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEmpty();
    }

    @Test
    @DisplayName("특수 문자가 포함된 이름으로 User를 저장하고 조회한다")
    void save_SpecialCharactersName_Success() {
        // given
        String specialName = "홍길동-O'Brien";
        User user = User.builder()
                .email("special@example.com")
                .name(specialName)
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByName(specialName);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("매우 긴 이메일 주소로 User를 저장한다")
    void save_LongEmail_Success() {
        // given
        String longEmail = "very.long.email.address.for.testing.purposes@example.com";
        User user = User.builder()
                .email(longEmail)
                .name("Long Email User")
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("User 정보를 업데이트한다")
    void update_Success() {
        // given
        User user = User.builder()
                .email("original@example.com")
                .name("Original Name")
                .build();
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // when
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        User updatedUser = User.builder()
                .id(foundUser.getId())
                .email("updated@example.com")
                .name("Updated Name")
                .build();
        userRepository.save(updatedUser);
        entityManager.flush();
        entityManager.clear();

        User finalUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // then
        assertThat(finalUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(finalUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("한글 이름으로 User를 저장하고 조회한다")
    void save_KoreanName_Success() {
        // given
        User user = User.builder()
                .email("korean@example.com")
                .name("김철수")
                .build();

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByName("김철수");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("김철수");
    }
}