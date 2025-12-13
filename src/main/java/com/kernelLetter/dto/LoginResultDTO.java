package com.kernelLetter.dto;

import com.kernelLetter.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;


// 카카오 로그인 결과를 담는 DTO
// 첫 로그인인지, 일반 로그인인지 구분하여 프론트엔드에 전달
@Getter
@AllArgsConstructor
public class LoginResultDTO {

    private boolean firstLogin;
    private String message;

    // 로그인한 사용자 정보 (첫 로그인이면 null)
    private SessionUser user;


    // 첫 로그인 응답 생성 (추가 정보 입력이 필요한 경우 사용)
    // @return 첫 로그인 응답 객체
    public static LoginResultDTO firstLogin() {
        return new LoginResultDTO(
                true,"추가 정보 입력 필요", null);
    }


    // 이미 추가 정보를 입력한 사용자의 로그인 성공 응답
    // @param user DB 에서 조회한 User 엔티티
    // @return 로그인 성공 응답 객체
    public static LoginResultDTO normalLogin(User user) {
        return new LoginResultDTO(
                false,"로그인 성공", SessionUser.fromUser(user));
    }
}