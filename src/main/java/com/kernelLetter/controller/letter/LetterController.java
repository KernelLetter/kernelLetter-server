package com.kernelLetter.controller.letter;

import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Letter")
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    @PostMapping
    public void sendLetter(@RequestBody LetterSendDto dto) {
        letterService.sendLetter(dto);
    }

    // 편지 수정하기
    @PatchMapping("/{receiverId}")
    public void update

    // 편지 삭제하기


    // 편지 조회하기


}
