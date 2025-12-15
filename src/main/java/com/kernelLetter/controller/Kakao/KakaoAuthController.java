package com.kernelLetter.controller.Kakao;

import com.kernelLetter.dto.KakaoUserInfoDTO;
import com.kernelLetter.dto.LoginResultDTO;
import com.kernelLetter.dto.SessionUser;
import com.kernelLetter.dto.UserRegisterDTO;
import com.kernelLetter.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;


// 카카오 로그인 관련 REST API 컨트롤러
@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    /**
     * 카카오 로그인 콜백 엔드포인트
     *
     * 카카오 로그인 완료 후 리다이렉트되는 주소
     *
     * @param code 카카오에서 발급한 인가코드
     * @return 프론트엔드 페이지로 리다이렉트
     */
    // 카카오 로그인 콜백 엔드포인트
    // @param code 카카오에서 발급한 인가코드
    // @return 프론트엔드 페이지로 리다이렉트
    @GetMapping("/auth/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam("code") String code) {

        // 1. 인가코드로 Access Token 발급
        String accessToken = kakaoAuthService.requestAccessToken(code);

        // 2. Access Token 으로 사용자 정보 조회
        KakaoUserInfoDTO kakaoUserInfo = kakaoAuthService.requestUserInfo(accessToken);

        // 3. 로그인 처리 (회원가입 또는 기존 사용자 로그인)
        LoginResultDTO result = kakaoAuthService.processLogin(kakaoUserInfo);

        // 4. 첫 로그인이면 추가 정보 입력 페이지로, 아니면 메인 페이지로 리다이렉트
        if (result.isFirstLogin()) {
            return new RedirectView("https://kernel-letter-fe.vercel.app/register");
        } else {
            return new RedirectView("https://kernel-letter-fe.vercel.app/");
        }
    }

    /**
     * 추가 정보 입력 엔드포인트 (첫 로그인 시)
     *
     * 프론트엔드에서 이름과 이메일을 입력받아 전송
     *
     * @param registerDTO 이름, 이메일 정보
     * @return 등록 완료 메시지
     */
    @PostMapping("/api/user/register")
    public ResponseEntity<String> registerAdditionalInfo(@RequestBody UserRegisterDTO registerDTO) {

        // 추가 정보 등록 및 세션 생성
        kakaoAuthService.registerAdditionalInfo(registerDTO.getName(), registerDTO.getEmail());

        return ResponseEntity.ok("등록이 완료되었습니다.");
    }

    /**
     * 로그아웃 엔드포인트
     *
     * @return 로그아웃 완료 메시지
     */
    @PostMapping("/api/user/logout")
    public ResponseEntity<String> logout() {
        kakaoAuthService.logout();
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    /**
     * 현재 로그인 사용자 정보 조회
     *
     * @return 세션에 저장된 사용자 정보
     */
    @GetMapping("/api/user/me")
    public ResponseEntity<?> getCurrentUser() {
        SessionUser currentUser = kakaoAuthService.getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        return ResponseEntity.ok(currentUser);
    }
}