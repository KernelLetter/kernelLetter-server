# 커널레터 개선 및 추가 기능 제안서

## 📋 프로젝트 현황

### 현재 구현된 핵심 기능
- ✅ 카카오 OAuth 로그인/회원가입
- ✅ 편지 작성/수정/삭제/조회
- ✅ 사용자 세션 관리
- ✅ Docker 기반 배포 환경
- ✅ MySQL + Redis 인프라

### 기술 스택
- Spring Boot 3.5.7 + Java 21
- MySQL 8.0, Redis 7
- Docker Compose
- GitHub Actions CI/CD

---

## 🎯 우선순위별 개선 및 추가 기능

## 1️⃣ 높은 우선순위 (High Priority)

### 1.1 사용자 프로필 관리 시스템
**현재 상태**: UserController가 비어있음, 프로필 관리 기능 없음

**제안 기능**:
- 사용자 프로필 조회 API (`GET /api/user/profile`)
- 프로필 정보 수정 API (`PATCH /api/user/profile`)
  - 이름, 소개글, 프로필 이미지 URL
- 사용자 검색 기능 (`GET /api/user/search?query={name}`)
- 내가 받은/보낸 편지 통계 조회

**예상 작업량**: 2-3일
**파일 위치**: `UserController.java`, `UserService.java`, `UserDto.java`

---

### 1.2 친구/연결 시스템
**현재 상태**: 누구나 누구에게든 편지를 보낼 수 있음

**제안 기능**:
- 친구 추가/삭제 기능
- 친구 목록 조회
- 친구 요청 수락/거절
- 친구에게만 편지 작성 허용 옵션
- 친구 추천 기능 (공통 친구 기반)

**DB 스키마**:
```sql
CREATE TABLE friendship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'),
    created_at DATETIME,
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (addressee_id) REFERENCES users(id)
);
```

**예상 작업량**: 3-4일
**파일 위치**: `FriendshipController.java`, `FriendshipService.java`, `Friendship.java`

---

### 1.3 알림 시스템 (이메일)
**현재 상태**: User 엔티티에 email 필드가 있지만 사용되지 않음

**제안 기능**:
- 편지 받았을 때 이메일 알림
- 알림 설정 관리 (켜기/끄기)
- 이메일 템플릿 디자인
- 친구 요청 알림

**기술 스택**:
- Spring Boot Starter Mail
- 또는 외부 서비스 (SendGrid, AWS SES)

**예상 작업량**: 2-3일
**파일 위치**: `EmailService.java`, `NotificationService.java`, `email-templates/`

---

### 1.4 편지 공유 기능
**현재 상태**: 편지는 본인만 조회 가능

**제안 기능**:
- 롤링페이퍼 페이지 공유 링크 생성
  - 예: `https://kernelletter.com/share/{shareToken}`
- 공유 토큰 기반 접근 제어
- 공유 설정 (공개/비공개)
- 공유 만료 기간 설정

**DB 스키마**:
```sql
CREATE TABLE share_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at DATETIME,
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**예상 작업량**: 2일
**파일 위치**: `ShareController.java`, `ShareService.java`, `ShareToken.java`

---

### 1.5 파일 업로드 (이미지/스티커)
**현재 상태**: 텍스트만 지원

**제안 기능**:
- 편지에 이미지 첨부 (최대 3장)
- 스티커/이모티콘 추가
- 프로필 이미지 업로드
- 이미지 크기 제한 및 압축
- S3 또는 로컬 스토리지 저장

**기술 스택**:
- AWS S3 또는 MinIO (self-hosted)
- Spring Boot Starter Web (MultipartFile)

**예상 작업량**: 3-4일
**파일 위치**: `FileUploadController.java`, `FileService.java`, `LetterImage.java`

---

## 2️⃣ 중간 우선순위 (Medium Priority)

### 2.1 편지 테마/템플릿 시스템
**제안 기능**:
- 다양한 편지 배경 템플릿
- 폰트 스타일 선택
- 색상 테마 선택
- 계절/이벤트별 테마

**DB 스키마**:
```sql
ALTER TABLE letter ADD COLUMN theme_id BIGINT;
ALTER TABLE letter ADD COLUMN font_style VARCHAR(50);
ALTER TABLE letter ADD COLUMN background_color VARCHAR(20);
```

**예상 작업량**: 2-3일

---

### 2.2 편지 검색 및 필터링
**제안 기능**:
- 발신자 이름으로 검색
- 내용으로 검색 (전문 검색)
- 날짜 범위 필터
- 읽음/안읽음 상태 필터

**기술 스택**:
- JPA Specification 또는 QueryDSL
- 또는 Elasticsearch (고급 검색)

**예상 작업량**: 2일

---

### 2.3 편지 프라이버시 설정
**제안 기능**:
- 편지 공개/비공개 설정
- 특정 사용자만 볼 수 있는 편지
- 편지 삭제 권한 (발신자/수신자)

**DB 스키마**:
```sql
ALTER TABLE letter ADD COLUMN visibility ENUM('PUBLIC', 'PRIVATE', 'FRIENDS_ONLY');
ALTER TABLE letter ADD COLUMN is_anonymous BOOLEAN DEFAULT FALSE;
```

**예상 작업량**: 1-2일

---

### 2.4 사용자 차단 기능
**제안 기능**:
- 특정 사용자 차단
- 차단된 사용자는 편지 전송 불가
- 차단 목록 관리

**DB 스키마**:
```sql
CREATE TABLE user_block (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (blocker_id) REFERENCES users(id),
    FOREIGN KEY (blocked_id) REFERENCES users(id)
);
```

**예상 작업량**: 1-2일

---

### 2.5 편지 반응 (좋아요/이모지)
**제안 기능**:
- 받은 편지에 이모지 반응 추가
- 좋아요 카운트
- 반응 종류: ❤️ 👍 😊 😢 등

**DB 스키마**:
```sql
CREATE TABLE letter_reaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    letter_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type VARCHAR(20),
    created_at DATETIME,
    FOREIGN KEY (letter_id) REFERENCES letter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**예상 작업량**: 1-2일

---

### 2.6 페이지네이션
**현재 상태**: 모든 편지를 한 번에 반환

**제안 기능**:
- 편지 목록 페이지네이션 (`GET /Letter/{userId}/all?page=0&size=20`)
- 무한 스크롤 지원을 위한 커서 기반 페이지네이션
- 정렬 옵션 (최신순/오래된순)

**예상 작업량**: 1일
**파일 위치**: `LetterController.java`, `LetterService.java`

---

## 3️⃣ 낮은 우선순위 (Low Priority)

### 3.1 통계 대시보드
**제안 기능**:
- 받은 편지/보낸 편지 통계
- 월별 편지 그래프
- 가장 많이 교류한 친구
- 편지 작성 빈도 분석

**예상 작업량**: 2-3일

---

### 3.2 편지 내보내기 (PDF/이미지)
**제안 기능**:
- 받은 편지를 PDF로 다운로드
- 이미지(PNG)로 저장
- 전체 롤링페이퍼 페이지 캡처

**기술 스택**:
- iText (PDF 생성)
- Html2Canvas (프론트엔드 이미지 변환)

**예상 작업량**: 3일

---

### 3.3 관리자 패널
**제안 기능**:
- 부적절한 콘텐츠 신고 처리
- 사용자 관리 (정지/복구)
- 시스템 통계 조회
- 로그 모니터링

**예상 작업량**: 4-5일

---

### 3.4 다국어 지원 (i18n)
**제안 기능**:
- 한국어/영어 지원
- Spring MessageSource 활용
- 에러 메시지 다국어화

**예상 작업량**: 2-3일

---

### 3.5 실시간 알림 (WebSocket)
**제안 기능**:
- 편지 도착 시 실시간 푸시 알림
- 친구 요청 실시간 알림
- WebSocket 연결 관리

**기술 스택**:
- Spring WebSocket
- STOMP 프로토콜

**예상 작업량**: 3-4일

---

## 🔧 기술적 개선 사항

### 보안 강화 (🔴 중요)
**현재 문제**: 모든 엔드포인트가 `permitAll()` 설정됨

**개선 방안**:
1. **JWT 인증 도입**
   - 카카오 로그인 후 JWT 토큰 발급
   - Refresh Token으로 토큰 갱신
   - Redis에 토큰 저장 (현재 Redis가 미사용 상태)

2. **권한 기반 접근 제어 (RBAC)**
   - 역할: USER, ADMIN
   - 각 엔드포인트에 `@PreAuthorize` 적용

3. **입력 검증 강화**
   - DTO에 `@Valid` 어노테이션 추가
   - `@NotNull`, `@Size`, `@Email` 등 검증 어노테이션 사용

4. **HTTPS 강제**
   - 프로덕션 환경에서 HTTPS만 허용
   - HSTS 헤더 설정

5. **API Rate Limiting**
   - Redis를 활용한 요청 횟수 제한
   - IP 기반 또는 사용자 기반 제한

**예상 작업량**: 3-4일

---

### API 문서화
**현재 상태**: API 문서 없음

**개선 방안**:
- **SpringDoc OpenAPI (Swagger) 도입**
  - 자동 API 문서 생성
  - Swagger UI 제공
  - 엔드포인트 테스트 가능

**추가 dependency**:
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

**예상 작업량**: 1일

---

### Redis 활용
**현재 상태**: Docker Compose에 설정되어 있으나 코드에서 미사용

**활용 방안**:
1. **세션 저장소**
   - HTTP Session을 Redis에 저장 (분산 환경 대응)
   - `spring-session-data-redis` 사용

2. **캐싱**
   - 사용자 정보 캐싱
   - 편지 목록 캐싱 (자주 조회되는 데이터)
   - `@Cacheable` 어노테이션 활용

3. **Rate Limiting**
   - API 요청 횟수 제한에 활용

**예상 작업량**: 2일

---

### 테스트 커버리지 향상
**현재 상태**: 5개 테스트 파일 존재

**개선 방안**:
- Service 계층 단위 테스트 추가
- Controller 통합 테스트 확대
- Repository 테스트 추가
- Test Coverage 80% 이상 목표
- JaCoCo 플러그인으로 커버리지 측정

**예상 작업량**: 지속적 진행

---

### 로깅 및 모니터링
**제안 사항**:
1. **구조화된 로깅**
   - Logback 설정 최적화
   - 로그 레벨 관리 (dev: DEBUG, prod: INFO)
   - 요청/응답 로깅

2. **모니터링 도구**
   - Spring Boot Actuator 활성화
   - Prometheus + Grafana 연동
   - 애플리케이션 메트릭 수집

**예상 작업량**: 2-3일

---

### 성능 최적화
**제안 사항**:
1. **N+1 쿼리 해결**
   - Letter 조회 시 Fetch Join 사용
   - `@EntityGraph` 활용

2. **인덱스 최적화**
   - `letter` 테이블에 `(receiver_id, created_at)` 복합 인덱스
   - `users` 테이블에 `kakaoId` 인덱스

3. **쿼리 최적화**
   - 불필요한 `SELECT *` 제거
   - DTO Projection 활용

**예상 작업량**: 1-2일

---

## 📊 우선순위 로드맵 (제안)

### Phase 1 (1-2주)
- ✅ 보안 강화 (JWT 인증)
- ✅ API 문서화 (Swagger)
- ✅ Redis 세션 저장소 활용
- ✅ 입력 검증 강화

### Phase 2 (2-3주)
- 🎯 사용자 프로필 관리
- 🎯 친구/연결 시스템
- 🎯 알림 시스템 (이메일)
- 🎯 페이지네이션

### Phase 3 (3-4주)
- 📸 파일 업로드 (이미지)
- 🔗 편지 공유 기능
- 🎨 편지 테마/템플릿
- 🔍 검색 및 필터링

### Phase 4 (지속적)
- 📈 통계 대시보드
- 🔔 실시간 알림 (WebSocket)
- 🌐 다국어 지원
- 👮 관리자 패널

---

## 🎨 프론트엔드 연계 고려사항

현재 백엔드만 분석했으나, 아래 기능들은 프론트엔드와 긴밀히 협업 필요:

1. **편지 테마/템플릿**: UI/UX 디자인 필수
2. **파일 업로드**: 이미지 미리보기, 드래그앤드롭
3. **실시간 알림**: WebSocket 클라이언트 구현
4. **공유 페이지**: 공개 페이지 디자인
5. **반응형 디자인**: 모바일 최적화

---

## 📝 참고 사항

### 현재 코드 품질
- ✅ 깔끔한 계층 구조 (Controller → Service → Repository)
- ✅ DTO 활용으로 데이터 전송 최적화
- ✅ 전역 예외 처리 구현
- ✅ Lombok으로 보일러플레이트 감소
- ✅ Docker 배포 환경 구축
- ✅ GitHub Actions CI/CD 파이프라인

### 개선이 필요한 부분
- ⚠️ 보안 설정이 너무 허용적 (permitAll)
- ⚠️ DTO 검증 어노테이션 부재
- ⚠️ UserService가 비어있음 (미사용)
- ⚠️ Redis 설정만 되어있고 미활용
- ⚠️ API 문서 부재
- ⚠️ 세션 기반 인증은 확장성 제한 (JWT 권장)

---

## 🚀 결론

커널레터 프로젝트는 견고한 기반 위에 구축되어 있으며, 롤링페이퍼의 핵심 기능은 잘 구현되어 있습니다.

**즉시 시작 가능한 작업**:
1. 보안 강화 (JWT + Redis)
2. API 문서화
3. 사용자 프로필 관리
4. 친구 시스템

이 순서로 진행하면 사용자 경험과 시스템 안정성을 크게 향상시킬 수 있습니다.

---

*생성일: 2025-12-13*
*분석 대상: kernelLetter-server*
*분석 도구: Claude Code (SuperClaude Framework)*
