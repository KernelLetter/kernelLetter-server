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

    @PostMapping("/{receiverId}")
    public void sendLetter(@PathVariable Long receiverId, @RequestBody LetterSendDto dto) {
        letterService.sendLetter(receiverId, dto);
    }

}
