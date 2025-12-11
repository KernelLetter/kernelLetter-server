package com.kernelLetter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 카카오에서 받아온 사용자 정보를 담는 DTO
// 카카오 API 에서 사용자 정보 조회 후 이 객체에 담아서 사용
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserInfoDTO {

    private String kakaoId;
    private String kakaoEmail;

    public void setKakaoId(String number) {
        this.kakaoId = number;
    }
    public void setKakaoEmail(String mail) {
        this.kakaoEmail = mail;
    }
}