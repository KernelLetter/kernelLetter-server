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
}
