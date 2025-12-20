package com.kernelLetter.service;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.LetterResponseDto;
import com.kernelLetter.dto.LetterPatchDto;
import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.global.error.exception.BusinessException;
import com.kernelLetter.global.error.ErrorCode;
import com.kernelLetter.repository.LetterRepository;
import com.kernelLetter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;

    public void sendLetter(LetterSendDto dto) {
        if (dto.getPosition() < 0 || dto.getPosition() > 39) {
            throw new BusinessException(ErrorCode.INVALID_POSITION); // ErrorCode에 추가 필요
        }

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        /*User receiver = userRepository.findBy(dto.getReceiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));*/

        // 이미 편지를 보냈는지 확인
        if (letterRepository.existsBySenderAndReceiverName(sender, dto.getReceiverName())) {
            throw new BusinessException(ErrorCode.SENDER_ALREADY_SENT_LETTER);
        }
        // 해당 위치에 이미 편지가 존재하는지 확인
        if (letterRepository.existsByReceiverNameAndPosition(dto.getReceiverName(), dto.getPosition())) {
            throw new BusinessException(ErrorCode.LETTER_ALREADY_EXISTS_AT_POSITION);
        }

        letterRepository.save(Letter.from(sender, dto.getReceiverName(), dto.getContent(), dto.getPosition()));
    }

    public void patch(Long receiverId, LetterPatchDto dto) {
        Letter letter = letterRepository.findBySenderIdAndReceiverId(dto.getSenderId(), receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LETTER_NOT_EXISTS));

        letter.setContent(dto);
    }

    public void delete(Long letterId) {

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LETTER_NOT_EXISTS));

        letterRepository.delete(letter);
    }

    @Transactional(readOnly = true)
    public List<LetterResponseDto> findAll(Long userId) {

        List<Letter> letters = letterRepository.findByReceiverId(userId);

        List<LetterResponseDto> list = letters.stream()
                .map(LetterResponseDto::from)
                .toList();

        return list;
    }

    @Transactional(readOnly = true)
    public LetterResponseDto find(Long userId, Long letterId) {

        Letter letter = letterRepository.findByReceiverIdAndId(userId, letterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LETTER_NOT_EXISTS));

        return LetterResponseDto.from(letter);
    }
}
