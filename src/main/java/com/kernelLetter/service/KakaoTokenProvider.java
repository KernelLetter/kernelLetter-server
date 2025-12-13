package com.kernelLetter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 인가코드를 Access Token 으로 교환하는 컴포넌트
 */
@Component
public class KakaoTokenProvider {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret}")
    private String clientSecret;


    // 카카오 인가코드를 Access Token 으로 교환
    // @param code 카카오 로그인 후 받은 인가코드
    // @return Access Token 문자열
    public String getAccessToken(String code) {

        // HTTP 요청을 보내기 위한 RestTemplate 생성
        RestTemplate restTemplate = new RestTemplate();

        // 카카오 토큰 발급 API 엔드포인트
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 파라미터 준비 (폼 데이터 형식)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        // 폼 데이터 형식으로 전송
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 객체 생성 (헤더 + 바디)
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // POST 요청 전송 및 응답 받기 (JSON을 String 으로 받음)
        ResponseEntity<String> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                String.class  // 응답을 문자열로 받음
        );

        // 응답 JSON 파싱하여 access_token 추출
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // JSON 문자열을 Map 으로 변환
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);

            // access_token 추출 및 반환
            String accessToken = (String) responseMap.get("access_token");

            if (accessToken == null) {
                throw new RuntimeException("카카오 토큰 발급 실패: access_token이 응답에 없습니다.");
            }

            return accessToken;

        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 파싱 실패", e);
        }
    }
}
