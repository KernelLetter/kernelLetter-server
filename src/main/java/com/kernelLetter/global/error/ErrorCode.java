package com.kernelLetter.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // test
    TEST(HttpStatus.BAD_REQUEST, "001", "test error"),

    // 인증 && 인가
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A-001", "토큰이 만료되었습니다."),
    NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "A-002", "해당 토큰은 유효한 토큰이 아닙니다."),
    NOT_EXISTS_AUTHORIZATION(HttpStatus.UNAUTHORIZED, "A-003", "Authorization Header가 빈값입니다."),
    NOT_VALID_BEARER_GRANT_TYPE(HttpStatus.UNAUTHORIZED, "A-004", "인증 타입이 Bearer 타입이 아닙니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A-005", "해당 refresh token은 존재하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A-006", "해당 refresh token은 만료되었습니다."),
    NOT_ACCESS_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "A-007", "해당 토큰은 access token이 아닙니다."),
    FORBIDDEN_ADMIN(HttpStatus.FORBIDDEN, "A-008", "관리자 Role이 아닙니다."),
    NOT_EQUAL_PASSWORD(HttpStatus.UNAUTHORIZED, "A-009", "Password가 일치하지 않습니다."),
    NOT_EQUAL_CODE(HttpStatus.UNAUTHORIZED, "A-010", "Email Code가 일치하지 않습니다."),
    NOT_VALID_USER(HttpStatus.BAD_REQUEST, "A-011", "해당 회원에게 접근 권한이 없습니다."),
    NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "A-012", "로그인이 필요합니다."),
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A-013", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // 회원
    INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "M-001", "잘못된 회원 타입입니다. (memberType : KAKAO)"),
    ALREADY_REGISTERED_USER(HttpStatus.BAD_REQUEST, "M-002", "이미 가입된 회원입니다."),
    USER_NOT_EXISTS(HttpStatus.BAD_REQUEST, "M-003", "해당 회원은 존재하지 않습니다."),
    NOT_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "M-004", "일치하는 Email 정보가 존재하지 않습니다."),
    ALREADY_EXISTS__EMAIL(HttpStatus.BAD_REQUEST, "M-005", "이미 존재하는 Email 입니다."),
    NOT_EXISTS_USER(HttpStatus.BAD_REQUEST, "M-006", "회원정보가 일치하지 않습니다."),
    USERNAME_IS_NULL(HttpStatus.BAD_REQUEST, "M-007", "username이 null 입니다."),
    NOT_ADMIN_USER(HttpStatus.BAD_REQUEST, "M-008", "Admin User가 아닙니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "M-009", "탈퇴한 회원입니다."),
    DORMANT_USER(HttpStatus.BAD_REQUEST, "M-010", "휴면 계정입니다."),

    // 편지
    LETTER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "L-002", "이미 해당 사용자에게 편지를 보냈습니다"),
    LETTER_NOT_EXISTS(HttpStatus.BAD_REQUEST, "L-003", "해당 편지가 존재하지 않습니다");
  
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
