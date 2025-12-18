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
public class LetterResponseDto {

    private String content;

    private String senderName;

    private int position;

    public static LetterResponseDto from(Letter letter){

        User user = letter.getSender();

        return LetterResponseDto.builder()
                .content(letter.getContent())
                .senderName(user.getName())
                .position(letter.getPosition())
                .build();
    }
}
