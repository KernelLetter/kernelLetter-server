package com.kernelLetter.service;

import com.kernelLetter.dto.KakaoUserInfoDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class KakaoUserInfoProvider {


    // Access Token을 사용하여 카카오 사용자 정보 조회
    // @param accessToken 카카오 Access Token
    // @return 카카오 사용자 정보 DTO
    public KakaoUserInfoDTO getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 카카오 사용자 정보 조회 API 엔드포인트
        String url = "https://kapi.kakao.com/v2/user/me";

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        // Authorization 헤더에 Bearer 토큰 형식으로 Access Token 추가
        headers.add("Authorization", "Bearer " + accessToken);

        // 응답 형식을 JSON 으로 지정
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 요청 객체 생성 (헤더만 있고 바디는 없음)
        HttpEntity<String> request = new HttpEntity<>(headers);

        // GET 요청 전송
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class  // 응답을 Map 으로 받음 (JSON 구조가 복잡하므로)
        );

        // 응답 데이터 파싱
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패: 응답이 비어있습니다.");
        }

        // 카카오 고유 ID 추출 (Long 타입)
        String kakaoId = String.valueOf(responseBody.get("id"));

        // 카카오 계정 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) responseBody.get("kakao_account");

        // 이메일 정보 추출 (없을 수도 있음)
        String kakaoEmail = kakaoAccount != null ?
                (String) kakaoAccount.get("email") : null;

        // DTO 생성 및 반환
        return KakaoUserInfoDTO.builder()
                .kakaoId(kakaoId)
                .kakaoEmail(kakaoEmail)
                .build();
    }
}
