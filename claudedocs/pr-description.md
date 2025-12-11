# Feature: 카카오 OAuth 2.0 로그인 구현

## 📋 개요
카카오 OAuth 2.0을 활용한 소셜 로그인 기능을 구현했습니다. 사용자는 카카오 계정으로 간편하게 로그인할 수 있으며, 첫 로그인 시 추가 정보(이름, 이메일)를 입력받아 서비스에 가입됩니다.

## 🎯 구현 기능

### 1. 카카오 OAuth 2.0 인증 플로우
- **인가 코드 → Access Token 교환**: 카카오 인증 서버로부터 받은 인가 코드를 Access Token으로 교환
- **사용자 정보 조회**: Access Token을 사용하여 카카오 사용자 정보(카카오 ID, 이메일) 조회
- **자동 회원가입/로그인**: 첫 방문 시 자동 회원가입, 재방문 시 자동 로그인

### 2. 2단계 회원가입 프로세스
**1단계: 카카오 로그인**
- 카카오 계정으로 인증
- 카카오 ID와 이메일 자동 수집
- DB에 기본 사용자 정보 저장 (isFirstLogin: true)

**2단계: 추가 정보 입력**
- 서비스에서 사용할 이름 입력
- 알림 수신용 이메일 입력
- 추가 정보 입력 완료 후 정식 로그인 (isFirstLogin: false)

### 3. 세션 기반 인증 관리
- **첫 로그인**: 세션에 `tempKakaoId` 저장 (임시 상태)
- **추가 정보 입력 후**: 세션에 `user` 객체 저장 (정식 로그인 상태)
- **로그아웃**: 세션 무효화

### 4. REST API 엔드포인트

#### `GET /auth/kakao/callback`
카카오 로그인 콜백 엔드포인트
- **Request**: `code` (카카오 인가 코드)
- **Response**:
  - 첫 로그인 → `302 Redirect` to `http://localhost:5173/register`
  - 재로그인 → `302 Redirect` to `http://localhost:5173/`

#### `POST /api/user/register`
추가 정보 입력 (첫 로그인 시)
- **Request Body**:
```json
{
  "name": "홍길동",
  "email": "user@example.com"
}
```
- **Response**: `200 OK` "등록이 완료되었습니다."

#### `GET /api/user/me`
현재 로그인 사용자 정보 조회
- **Response**:
```json
{
  "id": 1,
  "kakaoId": "123456789",
  "kakaoEmail": "user@kakao.com",
  "name": "홍길동",
  "email": "user@example.com"
}
```
- **Unauthorized**: `401` "로그인이 필요합니다."

#### `POST /api/user/logout`
로그아웃
- **Response**: `200 OK` "로그아웃되었습니다."

## 🏗️ 아키텍처

### 계층 구조
```
Controller (KakaoAuthController)
    ↓
Service (KakaoAuthService) ← 비즈니스 로직 중심
    ↓
├─ KakaoTokenProvider      (인가코드 → Access Token)
├─ KakaoUserInfoProvider   (Access Token → 사용자 정보)
└─ UserRepository          (DB 조회/저장)
```

### 주요 컴포넌트

#### 1. **KakaoAuthController**
- REST API 엔드포인트 제공
- 프론트엔드로 리다이렉션 처리
- 위치: `src/main/java/com/kernelLetter/controller/Kakao/KakaoAuthController.java`

#### 2. **KakaoAuthService**
- 로그인 프로세스 전체 오케스트레이션
- 세션 관리 (tempKakaoId, user)
- 사용자 등록/조회 로직
- 위치: `src/main/java/com/kernelLetter/service/KakaoAuthService.java`

#### 3. **KakaoTokenProvider**
- 카카오 토큰 발급 API 호출
- 인가코드 → Access Token 교환
- 위치: `src/main/java/com/kernelLetter/service/KakaoTokenProvider.java`

#### 4. **KakaoUserInfoProvider**
- 카카오 사용자 정보 조회 API 호출
- Access Token → 사용자 정보 파싱
- 위치: `src/main/java/com/kernelLetter/service/KakaoUserInfoProvider.java`

#### 5. **DTOs**
- `KakaoUserInfoDTO`: 카카오 사용자 정보 (kakaoId, kakaoEmail)
- `LoginResultDTO`: 로그인 결과 (firstLogin, message, user)
- `SessionUser`: 세션 저장용 사용자 정보 (Serializable)
- `UserRegisterDTO`: 추가 정보 입력 요청 (name, email)

## 🗄️ 데이터베이스 변경사항

### User 엔티티 확장
```java
@Entity
@Table(name = "users")  // H2 예약어 충돌 회피
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing
public class User {
    private String kakaoId;        // 카카오 고유 ID
    private String kakaoEmail;     // 카카오 계정 이메일
    private String name;           // 서비스 사용 이름
    private String email;          // 알림 수신 이메일
    private boolean isFirstLogin;  // 첫 로그인 여부

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Repository 메서드 추가
```java
Optional<User> findByKakaoId(String kakaoId);
```

## 🔧 환경 설정

### application-secret.yml 설정 필요
```yaml
kakao:
  client-id: YOUR_KAKAO_REST_API_KEY
  client-secret: YOUR_KAKAO_CLIENT_SECRET
  redirect-uri: http://localhost:8080/auth/kakao/callback
```

### 카카오 개발자 설정
1. [Kakao Developers](https://developers.kakao.com/) 애플리케이션 등록
2. **플랫폼 설정**: Web 플랫폼에 `http://localhost:5173` 추가
3. **Redirect URI**: `http://localhost:8080/auth/kakao/callback` 등록
4. **동의 항목 설정**:
   - 카카오 계정(이메일) - 필수 동의
   - 프로필 정보(닉네임) - 선택 동의
5. **보안**: Client Secret 발급 및 활성화

## 🎨 프론트엔드 연동 가이드

### 1. 카카오 로그인 버튼
```javascript
// 카카오 로그인 페이지로 리다이렉트
const handleKakaoLogin = () => {
  const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;
  window.location.href = KAKAO_AUTH_URL;
};
```

### 2. 콜백 처리 (자동)
- 백엔드가 `/auth/kakao/callback`에서 자동 처리
- 첫 로그인: `/register` 페이지로 리다이렉트
- 재로그인: 메인 페이지(`/`)로 리다이렉트

### 3. 추가 정보 입력 페이지 (`/register`)
```javascript
const handleRegister = async () => {
  const response = await fetch('/api/user/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email }),
    credentials: 'include'  // 세션 쿠키 포함
  });

  if (response.ok) {
    // 등록 완료 -> 메인 페이지로 이동
    navigate('/');
  }
};
```

### 4. 로그인 상태 확인
```javascript
const checkLoginStatus = async () => {
  const response = await fetch('/api/user/me', {
    credentials: 'include'
  });

  if (response.ok) {
    const user = await response.json();
    setUser(user);  // 로그인 상태
  } else {
    setUser(null);  // 비로그인 상태
  }
};
```

### 5. 로그아웃
```javascript
const handleLogout = async () => {
  await fetch('/api/user/logout', {
    method: 'POST',
    credentials: 'include'
  });

  setUser(null);
  navigate('/');
};
```

## 🧪 테스트

### 테스트 구조
총 23개의 테스트 케이스 작성 (모두 통과 ✅)

#### 1. 단위 테스트: `KakaoAuthControllerTest`
컨트롤러 레이어 독립 테스트 (Mock 기반)
```bash
./gradlew test --tests "KakaoAuthControllerTest"
```

**테스트 케이스:**
- ✅ 카카오 콜백 - 정상 로그인 (302 리다이렉트)
- ✅ 카카오 콜백 - 첫 로그인 (register 페이지로 리다이렉트)
- ✅ 카카오 콜백 - code 파라미터 누락 (400 Bad Request)
- ✅ 추가 정보 입력 - 성공
- ✅ 현재 사용자 조회 - 로그인된 상태
- ✅ 현재 사용자 조회 - 비로그인 상태 (401 Unauthorized)
- ✅ 로그아웃 - 성공

#### 2. 통합 테스트: `KakaoLoginIntegrationTest`
전체 애플리케이션 컨텍스트 로드하여 실제 플로우 테스트
```bash
./gradlew test --tests "KakaoLoginIntegrationTest"
```

**테스트 케이스:**
- ✅ 신규 사용자 첫 로그인부터 추가 정보 입력까지 전체 플로우
- ✅ 기존 사용자 재로그인
- ✅ 로그인하지 않고 사용자 정보 조회 시도
- ✅ 세션 없이 추가 정보 입력 시도
- ✅ DB에 사용자 정보 저장 확인

#### 3. 서비스 테스트: `KakaoAuthServiceTest`
비즈니스 로직 단위 테스트

### 전체 테스트 실행
```bash
# 카카오 관련 테스트만 실행
./gradlew test --tests "*Kakao*"

# 전체 테스트 실행
./gradlew test
```

### 테스트 설정 (application-test.yml)
- H2 인메모리 DB (MySQL 호환 모드)
- JPA DDL auto: create-drop
- Mock 카카오 API (실제 호출 없음)

## 🔒 보안 고려사항

### 1. 세션 기반 인증
- HttpOnly 쿠키 사용 (XSS 방지)
- Secure 쿠키 (HTTPS 환경)
- CSRF 토큰 (향후 추가 권장)

### 2. 민감 정보 관리
- Client Secret은 `application-secret.yml`에 분리 (gitignore)
- 환경 변수 또는 AWS Secrets Manager 사용 권장

### 3. 에러 처리
- 카카오 API 호출 실패 시 명확한 에러 메시지
- 세션 만료 시 재로그인 유도

## 📊 API 플로우 다이어그램

```
[프론트엔드]                [백엔드]                [카카오 API]
     |                         |                         |
     |  1. 로그인 버튼 클릭     |                         |
     |------------------------>|                         |
     |  2. 카카오 로그인 페이지로|                        |
     |<------------------------|                         |
     |                         |                         |
     |  3. 사용자 인증          |                         |
     |-------------------------------------------------->|
     |                         |                         |
     |  4. 인가코드 전달       |                         |
     |<--------------------------------------------------|
     |                         |                         |
     |  5. /auth/kakao/callback?code=xxx               |
     |------------------------>|                         |
     |                         |  6. Access Token 요청  |
     |                         |------------------------>|
     |                         |  7. Access Token 응답   |
     |                         |<------------------------|
     |                         |  8. 사용자 정보 요청    |
     |                         |------------------------>|
     |                         |  9. 사용자 정보 응답    |
     |                         |<------------------------|
     |                         | 10. DB 조회/저장        |
     |                         |                         |
     | 11a. 첫 로그인 → /register 리다이렉트           |
     |<------------------------|                         |
     | 11b. 재로그인 → / 리다이렉트                     |
     |<------------------------|                         |
     |                         |                         |
     | 12. POST /api/user/register (첫 로그인만)       |
     |------------------------>|                         |
     | 13. 등록 완료            |                         |
     |<------------------------|                         |
```

## 🐛 알려진 이슈 및 제한사항

### 현재 제한사항
1. **단일 소셜 로그인**: 카카오만 지원 (구글, 네이버 등 미지원)
2. **세션 스토리지**: 메모리 기반 (서버 재시작 시 세션 손실)
3. **CORS 설정**: localhost:5173만 허용 (프로덕션 환경 미설정)

### 향후 개선 사항
1. Redis 세션 스토어 도입 (분산 환경 대응)
2. JWT 기반 토큰 인증 전환 검토
3. 다중 소셜 로그인 지원 (구글, 네이버)
4. 리프레시 토큰 관리
5. 카카오 계정 연동 해제 API

## 📝 커밋 히스토리

### 주요 커밋
- `feat: 카카오 OAuth 2.0 로그인 기능 구현`
  - Controller, Service, Provider 계층 구현
  - 2단계 회원가입 프로세스 구현
  - 세션 기반 인증 관리

- `feat: 카카오 로그인 DTO 및 Entity 확장`
  - User 엔티티 카카오 필드 추가
  - LoginResultDTO, SessionUser, UserRegisterDTO 구현

- `test: 카카오 로그인 단위/통합 테스트 작성`
  - Controller 단위 테스트 7개
  - Integration 테스트 5개
  - Service 테스트 추가

- `fix: 카카오 로그인 테스트 수정 및 DB 설정 개선`
  - User 엔티티 JPA Auditing 활성화
  - H2 테스트 DB 설정 (MySQL 호환 모드)
  - Mock 설정 개선

## ✅ 체크리스트

- [x] 카카오 OAuth 2.0 인증 플로우 구현
- [x] 2단계 회원가입 프로세스 구현
- [x] 세션 기반 인증 관리
- [x] REST API 엔드포인트 구현
- [x] User 엔티티 확장 및 Repository 메서드 추가
- [x] 단위 테스트 작성 (7개)
- [x] 통합 테스트 작성 (5개)
- [x] 전체 테스트 통과 (23/23 ✅)
- [x] 환경 설정 문서화
- [x] 프론트엔드 연동 가이드 작성
- [ ] CORS 설정 (프로덕션 환경)
- [ ] Redis 세션 스토어 (향후)

## 🚀 배포 전 체크리스트

### 필수 설정
1. ✅ `application-secret.yml` 파일 생성 및 카카오 Client ID/Secret 설정
2. ✅ 카카오 개발자 콘솔에서 Redirect URI 등록
3. ⚠️ CORS 설정에 프로덕션 도메인 추가
4. ⚠️ HTTPS 설정 (Secure 쿠키)
5. ⚠️ 세션 타임아웃 설정 검토

### 권장 설정
1. Redis 세션 스토어 구성
2. 로그 레벨 조정 (프로덕션: INFO)
3. 에러 모니터링 설정
4. API Rate Limiting 검토

## 📚 참고 자료

- [Kakao Developers - REST API](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [Spring Security OAuth 2.0](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Spring Session](https://docs.spring.io/spring-session/reference/)

---

**구현 기간**: 2024-12-10 ~ 2024-12-11
**테스트 커버리지**: 23/23 (100% 통과)
**관련 이슈**: #(이슈 번호)