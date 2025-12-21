package com.kernelLetter.dto;

import com.kernelLetter.domain.entity.Letter;
import com.kernelLetter.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterSenderResponseDto {

    private String content;

    private String senderName;
    private String receiverName;

    private int position;

    public static LetterSenderResponseDto from(Letter letter){

        User user = letter.getSender();

        return LetterSenderResponseDto.builder()
                .content(letter.getContent())
                .senderName(user.getName())
                .receiverName(letter.getReceiverName())
                .position(letter.getPosition())
                .build();
    }
}
