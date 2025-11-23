package com.kernelLetter.service;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.LetterPatchDto;
import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.global.error.ErrorCode;
import com.kernelLetter.global.error.exception.BusinessException;
import com.kernelLetter.repository.LetterRepository;
import com.kernelLetter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LetterService 단위 테스트")
class LetterServiceTest {

    @Mock
    private LetterRepository letterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LetterService letterService;

    private User sender;
    private User receiver;
    private LetterSendDto letterSendDto;
    private LetterPatchDto letterPatchDto;
    private Letter letter;

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

        letterSendDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(2L)
                .content("안녕하세요! 테스트 편지입니다.")
                .build();

        letterPatchDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("수정된 편지 내용입니다.")
                .build();

        letter = Letter.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .content("원본 편지 내용입니다.")
                .build();
    }

    @Test
    @DisplayName("정상적으로 편지를 전송한다")
    void sendLetter_Success() {
        // given
        when(userRepository.findById(letterSendDto.getSenderId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(letterSendDto.getReceiverId())).thenReturn(Optional.of(receiver));
        when(letterRepository.save(any(Letter.class))).thenReturn(letter);

        // when
        letterService.sendLetter(letterSendDto);

        // then
        verify(userRepository, times(1)).findById(letterSendDto.getSenderId());
        verify(userRepository, times(1)).findById(letterSendDto.getReceiverId());
        verify(letterRepository, times(1)).save(any(Letter.class));
    }

    @Test
    @DisplayName("존재하지 않는 발신자 ID로 편지 전송 시 예외가 발생한다")
    void sendLetter_SenderNotFound_ThrowsException() {
        // given
        when(userRepository.findById(letterSendDto.getSenderId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> letterService.sendLetter(letterSendDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTS)
                .hasMessage(ErrorCode.USER_NOT_EXISTS.getMessage());

        verify(userRepository, times(1)).findById(letterSendDto.getSenderId());
        verify(userRepository, never()).findById(letterSendDto.getReceiverId());
        verify(letterRepository, never()).save(any(Letter.class));
    }

    @Test
    @DisplayName("존재하지 않는 수신자 ID로 편지 전송 시 예외가 발생한다")
    void sendLetter_ReceiverNotFound_ThrowsException() {
        // given
        when(userRepository.findById(letterSendDto.getSenderId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(letterSendDto.getReceiverId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> letterService.sendLetter(letterSendDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTS)
                .hasMessage(ErrorCode.USER_NOT_EXISTS.getMessage());

        verify(userRepository, times(1)).findById(letterSendDto.getSenderId());
        verify(userRepository, times(1)).findById(letterSendDto.getReceiverId());
        verify(letterRepository, never()).save(any(Letter.class));
    }

    @Test
    @DisplayName("null 값을 가진 DTO로 편지 전송 시 NullPointerException이 발생한다")
    void sendLetter_NullDto_ThrowsNullPointerException() {
        // when & then
        assertThatThrownBy(() -> letterService.sendLetter(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("빈 내용으로 편지를 전송한다")
    void sendLetter_EmptyContent_Success() {
        // given
        LetterSendDto emptyContentDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(2L)
                .content("")
                .build();

        when(userRepository.findById(emptyContentDto.getSenderId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(emptyContentDto.getReceiverId())).thenReturn(Optional.of(receiver));
        when(letterRepository.save(any(Letter.class))).thenReturn(letter);

        // when
        letterService.sendLetter(emptyContentDto);

        // then
        verify(letterRepository, times(1)).save(any(Letter.class));
    }

    @Test
    @DisplayName("매우 긴 내용의 편지를 전송한다")
    void sendLetter_LongContent_Success() {
        // given
        String longContent = "a".repeat(10000);
        LetterSendDto longContentDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(2L)
                .content(longContent)
                .build();

        when(userRepository.findById(longContentDto.getSenderId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(longContentDto.getReceiverId())).thenReturn(Optional.of(receiver));
        when(letterRepository.save(any(Letter.class))).thenReturn(letter);

        // when
        letterService.sendLetter(longContentDto);

        // then
        verify(letterRepository, times(1)).save(any(Letter.class));
    }

    @Test
    @DisplayName("같은 사용자가 발신자이자 수신자인 편지를 전송한다")
    void sendLetter_SelfLetter_Success() {
        // given
        LetterSendDto selfLetterDto = LetterSendDto.builder()
                .senderId(1L)
                .receiverId(1L)
                .content("자신에게 보내는 편지")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(letterRepository.save(any(Letter.class))).thenReturn(letter);

        // when
        letterService.sendLetter(selfLetterDto);

        // then
        verify(userRepository, times(2)).findById(1L);
        verify(letterRepository, times(1)).save(any(Letter.class));
    }

    @Test
    @DisplayName("정상적으로 편지를 수정한다")
    void patch_Success() {
        // given
        Long receiverId = 2L;
        when(letterRepository.findBySenderIdAndReceiverId(letterPatchDto.getSenderId(), receiverId))
                .thenReturn(Optional.of(letter));

        // when
        letterService.patch(receiverId, letterPatchDto);

        // then
        verify(letterRepository, times(1))
                .findBySenderIdAndReceiverId(letterPatchDto.getSenderId(), receiverId);
    }

    @Test
    @DisplayName("존재하지 않는 편지를 수정하려고 할 때 예외가 발생한다")
    void patch_LetterNotFound_ThrowsException() {
        // given
        Long receiverId = 2L;
        when(letterRepository.findBySenderIdAndReceiverId(letterPatchDto.getSenderId(), receiverId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> letterService.patch(receiverId, letterPatchDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LETTER_NOT_EXISTS)
                .hasMessage(ErrorCode.LETTER_NOT_EXISTS.getMessage());

        verify(letterRepository, times(1))
                .findBySenderIdAndReceiverId(letterPatchDto.getSenderId(), receiverId);
    }

    @Test
    @DisplayName("잘못된 발신자 ID로 편지 수정 시 예외가 발생한다")
    void patch_InvalidSenderId_ThrowsException() {
        // given
        Long receiverId = 2L;
        LetterPatchDto invalidSenderDto = LetterPatchDto.builder()
                .senderId(999L)
                .content("수정된 내용")
                .build();

        when(letterRepository.findBySenderIdAndReceiverId(invalidSenderDto.getSenderId(), receiverId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> letterService.patch(receiverId, invalidSenderDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LETTER_NOT_EXISTS);
    }

    @Test
    @DisplayName("null 수신자 ID로 편지 수정 시 예외가 발생한다")
    void patch_NullReceiverId_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> letterService.patch(null, letterPatchDto))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("null DTO로 편지 수정 시 예외가 발생한다")
    void patch_NullDto_ThrowsException() {
        // given
        Long receiverId = 2L;

        // when & then
        assertThatThrownBy(() -> letterService.patch(receiverId, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("빈 내용으로 편지를 수정한다")
    void patch_EmptyContent_Success() {
        // given
        Long receiverId = 2L;
        LetterPatchDto emptyContentDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("")
                .build();

        when(letterRepository.findBySenderIdAndReceiverId(emptyContentDto.getSenderId(), receiverId))
                .thenReturn(Optional.of(letter));

        // when
        letterService.patch(receiverId, emptyContentDto);

        // then
        verify(letterRepository, times(1))
                .findBySenderIdAndReceiverId(emptyContentDto.getSenderId(), receiverId);
    }

    @Test
    @DisplayName("특수 문자가 포함된 내용으로 편지를 수정한다")
    void patch_SpecialCharacters_Success() {
        // given
        Long receiverId = 2L;
        LetterPatchDto specialCharDto = LetterPatchDto.builder()
                .senderId(1L)
                .content("특수문자 테스트: !@#$%^&*()_+-=[]{}|;':\",./<>?")
                .build();

        when(letterRepository.findBySenderIdAndReceiverId(specialCharDto.getSenderId(), receiverId))
                .thenReturn(Optional.of(letter));

        // when
        letterService.patch(receiverId, specialCharDto);

        // then
        verify(letterRepository, times(1))
                .findBySenderIdAndReceiverId(specialCharDto.getSenderId(), receiverId);
    }
}