package com.kernelLetter.service;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.global.error.exception.BusinessException;
import com.kernelLetter.repository.LetterRepository;
import com.kernelLetter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kernelLetter.global.error.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;

    public void sendLetter(LetterSendDto dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));

        letterRepository.save(Letter.from(sender, receiver, dto.getContent()));
    }
}
