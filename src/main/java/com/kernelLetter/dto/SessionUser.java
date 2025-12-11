package com.kernelLetter.dto;

import com.kernelLetter.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionUser implements Serializable {

    //직렬화 버전 ID (클래스 변경 시 호환성 관리)
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String kakaoId;
    private String kakaoEmail;
    private String name;
    private String email;


    public static SessionUser fromUser(User user) {
        return SessionUser.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .kakaoEmail(user.getKakaoEmail())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
