package com.kernelLetter.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 처음 로그인한 사용자가 추가 정보를 입력할 때 사용하는 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDTO {

    private String name;
    private String email;

    public void setName(String testUser) {
        this.name = testUser;
    }

    public void setEmail(String mail) {
        this.email = mail;
    }
}
