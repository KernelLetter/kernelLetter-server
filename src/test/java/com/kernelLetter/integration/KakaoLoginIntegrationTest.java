package com.kernelLetter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.KakaoUserInfoDTO;
import com.kernelLetter.dto.UserRegisterDTO;
import com.kernelLetter.repository.UserRepository;
import com.kernelLetter.service.KakaoAuthService;
import com.kernelLetter.service.KakaoTokenProvider;
import com.kernelLetter.service.KakaoUserInfoProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 카카오 로그인 통합 테스트
 * 전체 애플리케이션 컨텍스트를 로드하여 실제 플로우 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class KakaoLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KakaoAuthService kakaoAuthService;

    @MockBean
    private KakaoTokenProvider kakaoTokenProvider;

    @MockBean
    private KakaoUserInfoProvider kakaoUserInfoProvider;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        userRepository.deleteAll();

        // Mock 카카오 API 응답 설정
        KakaoUserInfoDTO mockUserInfo = new KakaoUserInfoDTO();
        mockUserInfo.setKakaoId("test-kakao-123");
        mockUserInfo.setKakaoEmail("test@kakao.com");

        given(kakaoTokenProvider.getAccessToken(anyString())).willReturn("mock-access-token");
        given(kakaoUserInfoProvider.getKakaoUserInfo(anyString())).willReturn(mockUserInfo);
    }

    @Test
    @DisplayName("통합 테스트 - 신규 사용자 첫 로그인부터 추가 정보 입력까지")
    void fullLoginFlow_NewUser() throws Exception {
        // 1단계: 카카오 콜백 (첫 로그인)
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "test-code")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:5173/register"));

        // 세션에 tempKakaoId가 저장되었는지 확인
        assertThat(session.getAttribute("tempKakaoId")).isNotNull();

        // 2단계: 추가 정보 입력
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setName("테스트유저");
        registerDTO.setEmail("test@example.com");

        mockMvc.perform(post("/api/user/register")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("등록이 완료되었습니다."));

        // 세션에 user가 저장되고 tempKakaoId는 제거되었는지 확인
        assertThat(session.getAttribute("user")).isNotNull();
        assertThat(session.getAttribute("tempKakaoId")).isNull();

        // 3단계: 현재 사용자 정보 조회
        mockMvc.perform(get("/api/user/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // 4단계: 로그아웃
        mockMvc.perform(post("/api/user/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃되었습니다."));

        // 세션이 무효화되었는지 확인
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("통합 테스트 - 기존 사용자 재로그인")
    void fullLoginFlow_ExistingUser() throws Exception {
        // 준비: 기존 사용자 생성
        User existingUser = User.builder()
                .kakaoId("existing-123456")
                .kakaoEmail("existing@kakao.com")
                .name("기존유저")
                .email("existing@example.com")
                .isFirstLogin(false)
                .build();
        userRepository.save(existingUser);

        // Mock을 기존 사용자 정보로 재설정
        KakaoUserInfoDTO existingUserInfo = new KakaoUserInfoDTO();
        existingUserInfo.setKakaoId("existing-123456");
        existingUserInfo.setKakaoEmail("existing@kakao.com");
        given(kakaoUserInfoProvider.getKakaoUserInfo(anyString())).willReturn(existingUserInfo);

        // 카카오 콜백으로 로그인
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "test-code")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:5173/"));

        // 세션에 user가 바로 저장되었는지 확인
        assertThat(session.getAttribute("user")).isNotNull();
        assertThat(session.getAttribute("tempKakaoId")).isNull();
    }

    @Test
    @DisplayName("통합 테스트 - 로그인하지 않고 사용자 정보 조회 시도")
    void getCurrentUser_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("통합 테스트 - 세션 없이 추가 정보 입력 시도")
    void registerAdditionalInfo_NoSession() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setName("테스트유저");
        registerDTO.setEmail("test@example.com");

        mockMvc.perform(post("/api/user/register")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("통합 테스트 - DB에 사용자 정보 저장 확인")
    void verifyUserPersistence() throws Exception {
        // 카카오 콜백으로 신규 사용자 생성
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "test-code")
                        .session(session))
                .andExpect(status().is3xxRedirection());

        // DB에 사용자가 저장되었는지 확인
        assertThat(userRepository.count()).isEqualTo(1);
        User savedUser = userRepository.findAll().get(0);
        assertThat(savedUser.getKakaoId()).isNotNull();
        assertThat(savedUser.isFirstLogin()).isTrue();

        // 추가 정보 입력
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setName("테스트유저");
        registerDTO.setEmail("test@example.com");

        mockMvc.perform(post("/api/user/register")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // DB 에서 업데이트된 정보 확인
        User updatedUser = userRepository.findAll().getFirst();
        assertThat(updatedUser.getName()).isEqualTo("테스트유저");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(updatedUser.isFirstLogin()).isFalse();
    }
}