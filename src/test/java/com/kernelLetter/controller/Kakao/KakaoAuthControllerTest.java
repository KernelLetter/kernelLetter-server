package com.kernelLetter.controller.Kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.KakaoUserInfoDTO;
import com.kernelLetter.dto.LoginResultDTO;
import com.kernelLetter.dto.SessionUser;
import com.kernelLetter.dto.UserRegisterDTO;
import com.kernelLetter.service.KakaoAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KakaoAuthController 단위 테스트
 * MockMvc를 사용하여 컨트롤러 레이어만 독립적으로 테스트
 */
@WebMvcTest(controllers = KakaoAuthController.class)
@ContextConfiguration(classes = {KakaoAuthController.class, TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class KakaoAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KakaoAuthService kakaoAuthService;

    private String testCode;
    private String testAccessToken;
    private KakaoUserInfoDTO testKakaoUserInfo;
    private LoginResultDTO testLoginResult;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCode = "test-authorization-code";
        testAccessToken = "test-access-token";

        testKakaoUserInfo = new KakaoUserInfoDTO();
        testKakaoUserInfo.setKakaoId("123456789");
        testKakaoUserInfo.setKakaoEmail("test@kakao.com");

        testUser = User.builder()
                .id(1L)
                .kakaoId("123456789")
                .kakaoEmail("test@kakao.com")
                .name("테스트유저")
                .email("test@example.com")
                .isFirstLogin(false)
                .build();

        testLoginResult = LoginResultDTO.normalLogin(testUser);
    }

    @Test
    @DisplayName("카카오 콜백 - 정상 로그인")
    void kakaoCallback_Success() throws Exception {
        // given
        given(kakaoAuthService.requestAccessToken(testCode)).willReturn(testAccessToken);
        given(kakaoAuthService.requestUserInfo(testAccessToken)).willReturn(testKakaoUserInfo);
        given(kakaoAuthService.processLogin(any(KakaoUserInfoDTO.class))).willReturn(testLoginResult);

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", testCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:5173/"));

        // verify
        verify(kakaoAuthService, times(1)).requestAccessToken(testCode);
        verify(kakaoAuthService, times(1)).requestUserInfo(testAccessToken);
        verify(kakaoAuthService, times(1)).processLogin(any(KakaoUserInfoDTO.class));
    }

    @Test
    @DisplayName("카카오 콜백 - 첫 로그인")
    void kakaoCallback_FirstLogin() throws Exception {
        // given
        LoginResultDTO firstLoginResult = LoginResultDTO.firstLogin();
        given(kakaoAuthService.requestAccessToken(anyString())).willReturn(testAccessToken);
        given(kakaoAuthService.requestUserInfo(anyString())).willReturn(testKakaoUserInfo);
        given(kakaoAuthService.processLogin(any(KakaoUserInfoDTO.class))).willReturn(firstLoginResult);

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", testCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:5173/register"));
    }

    @Test
    @DisplayName("추가 정보 입력 - 성공")
    void registerAdditionalInfo_Success() throws Exception {
        // given
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setName("테스트유저");
        registerDTO.setEmail("test@example.com");

        doNothing().when(kakaoAuthService).registerAdditionalInfo(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("등록이 완료되었습니다."));

        // verify
        verify(kakaoAuthService, times(1)).registerAdditionalInfo("테스트유저", "test@example.com");
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() throws Exception {
        // given
        doNothing().when(kakaoAuthService).logout();

        // when & then
        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃되었습니다."));

        // verify
        verify(kakaoAuthService, times(1)).logout();
    }

    @Test
    @DisplayName("현재 사용자 조회 - 로그인된 상태")
    void getCurrentUser_LoggedIn() throws Exception {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .kakaoId("123456789")
                .name("테스트유저")
                .email("test@example.com")
                .build();

        given(kakaoAuthService.getCurrentUser()).willReturn(sessionUser);

        // when & then
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kakaoId").value("123456789"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("현재 사용자 조회 - 로그인되지 않은 상태")
    void getCurrentUser_NotLoggedIn() throws Exception {
        // given
        given(kakaoAuthService.getCurrentUser()).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("카카오 콜백 - code 파라미터 누락")
    void kakaoCallback_MissingCode() throws Exception {
        // when & then
        mockMvc.perform(get("/auth/kakao/callback"))
                .andExpect(status().isBadRequest());
    }
}