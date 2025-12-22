package com.kernelLetter.controller.letter;

import com.kernelLetter.dto.LetterResponseDto;
import com.kernelLetter.dto.LetterPatchDto;
import com.kernelLetter.dto.LetterSendDto;
import com.kernelLetter.dto.LetterSenderResponseDto;
import com.kernelLetter.service.LetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Letter")
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    // 편지 작성하기
    @PostMapping
    public void sendLetter(@RequestBody LetterSendDto dto) {
        letterService.sendLetter(dto);
    }

    // 편지 수정하기
    @PatchMapping("/{receiverId}")
    public ResponseEntity<String> update(@PathVariable String receiverName,
                                         @RequestBody LetterPatchDto dto) {
        letterService.patch(receiverName,dto);

        return ResponseEntity.ok("수정이 완료되었습니다.");
    }

    // 편지 삭제하기
    @DeleteMapping("/{letterId}")
    public ResponseEntity<String> delete(@PathVariable Long letterId) {
        letterService.delete(letterId);

        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }

    // 받은 편지 전체 조회하기
    @GetMapping("/{receiverName}/all")
    public ResponseEntity<List<LetterResponseDto>> findAllLetters(@PathVariable String receiverName) {
        List<LetterResponseDto>list = letterService.findAll(receiverName);

        return ResponseEntity.ok(list);
    }

    // 받은 편지 하나 조회하기
    @GetMapping("/{userId}/{letterId}")
    public ResponseEntity<LetterResponseDto> findOneLetter(@PathVariable Long userId, @PathVariable Long letterId) {
        LetterResponseDto letter = letterService.find(userId, letterId);

        return ResponseEntity.ok(letter);
    }

    // 받은 편지 전체 조회하기
    @GetMapping("/send/{userId}/all")
    public ResponseEntity<List<LetterSenderResponseDto>> findAllSendLetters(@PathVariable Long userId) {
        List<LetterSenderResponseDto>list = letterService.findAllByUserId(userId);

        return ResponseEntity.ok(list);
    }
}
