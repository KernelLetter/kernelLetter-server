package com.kernelLetter.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 첫 로그인 사용자(정보 미입력)의 API 접근을 제한하는 인터셉터
 *
 * 세션에 tempKakaoId가 있는 경우(isFirstLogin=true)
 * /user/register 외의 모든 API 접근을 차단합니다.
 */
@Component
public class FirstLoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 세션이 없으면 통과 (비로그인 상태)
        if (session == null) {
            return true;
        }

        // tempKakaoId가 있으면 정보 미입력 사용자
        String tempKakaoId = (String) session.getAttribute("tempKakaoId");

        if (tempKakaoId != null) {
            // 허용된 경로인지 확인
            String requestURI = request.getRequestURI();

            // 허용 경로: 정보 입력, 카카오 콜백, 로그아웃
            if (requestURI.equals("/user/register") ||
                requestURI.equals("/auth/kakao/callback") ||
                requestURI.equals("/user/logout")) {
                return true;
            }

            // 그 외 모든 경로는 차단
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"추가 정보 입력이 필요합니다. /user/register 에서 정보를 입력해주세요.\"}");
            return false;
        }

        // tempKakaoId가 없으면 정식 로그인 또는 비로그인 → 통과
        return true;
    }
}