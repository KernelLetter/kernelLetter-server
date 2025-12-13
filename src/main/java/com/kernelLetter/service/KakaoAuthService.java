package com.kernelLetter.service;

import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.KakaoUserInfoDTO;
import com.kernelLetter.dto.LoginResultDTO;
import com.kernelLetter.dto.SessionUser;
import com.kernelLetter.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


//카카오 로그인 전체 프로세스를 관리하는 서비스

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final KakaoTokenProvider kakaoTokenProvider;
    private final KakaoUserInfoProvider kakaoUserInfoProvider;
    private final HttpSession httpSession;


    // 1단계: 인가코드로 Access Token 요청
    // @param code 카카오 인가코드
    // @return Access Token
    public String requestAccessToken(String code) {
        return kakaoTokenProvider.getAccessToken(code);
    }


    // 2단계: Access Token 으로 사용자 정보 조회
    // @param accessToken 카카오 Access Token
    // @return 카카오 사용자 정보
    public KakaoUserInfoDTO requestUserInfo(String accessToken) {
        return kakaoUserInfoProvider.getKakaoUserInfo(accessToken);
    }


    // 3단계: 로그인 처리 (회원가입 또는 로그인)
    // @param kakaoUserInfo 카카오에서 받아온 사용자 정보
    // @return 로그인 결과 (첫 로그인 여부 포함)
    @Transactional
    public LoginResultDTO processLogin(KakaoUserInfoDTO kakaoUserInfo) {

        // DB 에서 카카오 ID로 사용자 검색
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId());

        // 이미 가입된 사용자인 경우
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 첫 로그인 여부 확인
            if (user.isFirstLogin()) {

                // 첫 로그인이면 추가 정보 입력 필요
                // 세션에는 임시로 카카오 정보만 저장
                httpSession.setAttribute("tempKakaoId", kakaoUserInfo.getKakaoId());
                return LoginResultDTO.firstLogin();
            }
            else {

                // 이미 추가 정보를 입력한 사용자
                // 세션에 사용자 정보 저장
                SessionUser sessionUser = SessionUser.fromUser(user);
                httpSession.setAttribute("user", sessionUser);
                return LoginResultDTO.normalLogin(user);
            }

        } else {
            // 처음 가입하는 사용자 → DB에 저장
            User newUser = User.builder()
                    .kakaoId(kakaoUserInfo.getKakaoId())
                    .kakaoEmail(kakaoUserInfo.getKakaoEmail())
                    .isFirstLogin(true)
                    .build();

            userRepository.save(newUser);

            // 세션에 임시로 카카오 ID 저장 (추가 정보 입력 대기)
            httpSession.setAttribute("tempKakaoId", kakaoUserInfo.getKakaoId());

            return LoginResultDTO.firstLogin();
        }
    }

    // 4단계: 추가 정보 입력 처리 (처음 로그인 시)
    // @param name 사용자 이름
    // @param email 알림받을 이메일
    @Transactional
    public void registerAdditionalInfo(String name, String email) {

        // 세션에서 임시 저장한 카카오 ID 가져오기
        String tempKakaoId = (String) httpSession.getAttribute("tempKakaoId");

        if (tempKakaoId == null) {
            throw new IllegalStateException("세션에 카카오 정보가 없습니다. 다시 로그인해주세요.");
        }

        // DB 에서 사용자 조회
        User user = userRepository.findByKakaoId(tempKakaoId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 추가 정보 업데이트
        user.updateEmail(name, email);

        // 세션에 정식 사용자 정보 저장
        SessionUser sessionUser = SessionUser.fromUser(user);
        httpSession.setAttribute("user", sessionUser);

        // 임시 카카오 ID는 제거
        httpSession.removeAttribute("tempKakaoId");
    }


    // 로그아웃 처리
    public void logout() {
        // 세션 무효화 (모든 세션 데이터 삭제)
        httpSession.invalidate();
    }


    // 현재 로그인한 사용자 정보 조회
    // @return 세션에 저장된 사용자 정보 (없으면 null)
    public SessionUser getCurrentUser() {
        return (SessionUser) httpSession.getAttribute("user");
    }
}