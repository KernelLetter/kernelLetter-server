package com.kernelLetter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LetterSendDto {
    private Long senderId;
    private Long receiverId;
    private String content;
}
