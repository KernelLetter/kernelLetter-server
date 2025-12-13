package com.kernelLetter.service;

import com.kernelLetter.domain.entity.User;
import com.kernelLetter.dto.KakaoUserInfoDTO;
import com.kernelLetter.dto.LoginResultDTO;
import com.kernelLetter.dto.SessionUser;
import com.kernelLetter.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * KakaoAuthService 단위 테스트
 * Mock 객체를 사용하여 서비스 레이어의 비즈니스 로직 테스트
 */
@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoTokenProvider kakaoTokenProvider;

    @Mock
    private KakaoUserInfoProvider kakaoUserInfoProvider;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    private String testCode;
    private String testAccessToken;
    private KakaoUserInfoDTO testKakaoUserInfo;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCode = "test-authorization-code";
        testAccessToken = "test-access-token";

        testKakaoUserInfo = new KakaoUserInfoDTO();
        testKakaoUserInfo.setKakaoId("123456789");
        testKakaoUserInfo.setKakaoEmail("test@kakao.com");

        testUser = User.builder()
                .kakaoId("123456789")
                .kakaoEmail("test@kakao.com")
                .name("테스트유저")
                .email("test@example.com")
                .isFirstLogin(false)
                .build();
    }

    @Test
    @DisplayName("Access Token 요청 - 성공")
    void requestAccessToken_Success() {
        // given
        given(kakaoTokenProvider.getAccessToken(testCode)).willReturn(testAccessToken);

        // when
        String result = kakaoAuthService.requestAccessToken(testCode);

        // then
        assertThat(result).isEqualTo(testAccessToken);
        verify(kakaoTokenProvider, times(1)).getAccessToken(testCode);
    }

    @Test
    @DisplayName("사용자 정보 요청 - 성공")
    void requestUserInfo_Success() {
        // given
        given(kakaoUserInfoProvider.getKakaoUserInfo(testAccessToken)).willReturn(testKakaoUserInfo);

        // when
        KakaoUserInfoDTO result = kakaoAuthService.requestUserInfo(testAccessToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getKakaoId()).isEqualTo("123456789");
        verify(kakaoUserInfoProvider, times(1)).getKakaoUserInfo(testAccessToken);
    }

    @Test
    @DisplayName("로그인 처리 - 첫 로그인 사용자")
    void processLogin_FirstLoginUser() {
        // given
        User firstLoginUser = User.builder()
                .kakaoId("123456789")
                .kakaoEmail("test@kakao.com")
                .isFirstLogin(true)
                .build();

        given(userRepository.findByKakaoId(anyString())).willReturn(Optional.of(firstLoginUser));

        // when
        LoginResultDTO result = kakaoAuthService.processLogin(testKakaoUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isFirstLogin()).isTrue();
        assertThat(result.getMessage()).isEqualTo("추가 정보 입력 필요");
        verify(httpSession, times(1)).setAttribute("tempKakaoId", "123456789");
    }

    @Test
    @DisplayName("로그인 처리 - 기존 사용자 정상 로그인")
    void processLogin_ExistingUser() {
        // given
        given(userRepository.findByKakaoId(anyString())).willReturn(Optional.of(testUser));

        // when
        LoginResultDTO result = kakaoAuthService.processLogin(testKakaoUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isFirstLogin()).isFalse();
        assertThat(result.getMessage()).isEqualTo("로그인 성공");
        verify(httpSession, times(1)).setAttribute(eq("user"), any(SessionUser.class));
    }

    @Test
    @DisplayName("로그인 처리 - 신규 사용자 회원가입")
    void processLogin_NewUser() {
        // given
        given(userRepository.findByKakaoId(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        LoginResultDTO result = kakaoAuthService.processLogin(testKakaoUserInfo);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isFirstLogin()).isTrue();
        verify(userRepository, times(1)).save(any(User.class));
        verify(httpSession, times(1)).setAttribute("tempKakaoId", "123456789");
    }

    @Test
    @DisplayName("추가 정보 입력 - 성공")
    void registerAdditionalInfo_Success() {
        // given
        String testKakaoId = "123456789";
        String testName = "테스트유저";
        String testEmail = "test@example.com";

        User firstLoginUser = User.builder()
                .kakaoId(testKakaoId)
                .kakaoEmail("test@kakao.com")
                .isFirstLogin(true)
                .build();

        given(httpSession.getAttribute("tempKakaoId")).willReturn(testKakaoId);
        given(userRepository.findByKakaoId(testKakaoId)).willReturn(Optional.of(firstLoginUser));

        // when
        kakaoAuthService.registerAdditionalInfo(testName, testEmail);

        // then
        verify(httpSession, times(1)).setAttribute(eq("user"), any(SessionUser.class));
        verify(httpSession, times(1)).removeAttribute("tempKakaoId");
    }

    @Test
    @DisplayName("추가 정보 입력 - 세션에 카카오 정보 없음")
    void registerAdditionalInfo_NoSession() {
        // given
        given(httpSession.getAttribute("tempKakaoId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> kakaoAuthService.registerAdditionalInfo("테스트", "test@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("세션에 카카오 정보가 없습니다. 다시 로그인해주세요.");
    }

    @Test
    @DisplayName("추가 정보 입력 - 사용자를 찾을 수 없음")
    void registerAdditionalInfo_UserNotFound() {
        // given
        given(httpSession.getAttribute("tempKakaoId")).willReturn("123456789");
        given(userRepository.findByKakaoId(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> kakaoAuthService.registerAdditionalInfo("테스트", "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() {
        // when
        kakaoAuthService.logout();

        // then
        verify(httpSession, times(1)).invalidate();
    }

    @Test
    @DisplayName("현재 사용자 조회 - 로그인된 상태")
    void getCurrentUser_LoggedIn() {
        // given
        SessionUser sessionUser = SessionUser.builder()
                .kakaoId("123456789")
                .build();
        given(httpSession.getAttribute("user")).willReturn(sessionUser);

        // when
        SessionUser result = kakaoAuthService.getCurrentUser();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getKakaoId()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("현재 사용자 조회 - 로그인되지 않은 상태")
    void getCurrentUser_NotLoggedIn() {
        // given
        given(httpSession.getAttribute("user")).willReturn(null);

        // when
        SessionUser result = kakaoAuthService.getCurrentUser();

        // then
        assertThat(result).isNull();
    }
}