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
public class LetterHideResponseDto {

    private String content;

    private String senderName;

    private int position;

    public static LetterHideResponseDto from(Letter letter){

        User user = letter.getSender();

        return LetterHideResponseDto.builder()
                .content("궁금하지~~?")
                .senderName("비밀~~~~")
                .position(letter.getPosition())
                .build();
    }
}
