# 카카오 로그인 테스트 가이드

## 📋 테스트 완료 현황

### ✅ 자동 테스트 (완료)
- **KakaoAuthServiceTest**: 모든 테스트 통과 ✅
  - Access Token 요청
  - 사용자 정보 요청
  - 로그인 처리 (첫 로그인, 기존 사용자, 신규 사용자)
  - 추가 정보 입력
  - 로그아웃
  - 현재 사용자 조회

### ⏳ 수동 테스트 (진행 예정)

## 🚀 수동 테스트 방법

### 1단계: 환경 준비

```bash
# 1. MySQL 실행 (이미 실행 중이면 생략)
docker-compose up -d

# 2. MySQL 연결 확인
docker ps

# 3. 애플리케이션 실행
./gradlew bootRun
```

### 2단계: API 엔드포인트 테스트

#### API 목록
1. `GET /auth/kakao/callback?code={code}` - 카카오 로그인 콜백
2. `POST /api/user/register` - 추가 정보 입력
3. `POST /api/user/logout` - 로그아웃
4. `GET /api/user/me` - 현재 사용자 정보

#### 방법 A: curl 사용

```bash
# 1. 현재 사용자 정보 조회 (로그인 전 - 401 예상)
curl -X GET http://localhost:8080/api/user/me

# 2. 로그아웃 (세션 무효화)
curl -X POST http://localhost:8080/api/user/logout

# 3. 추가 정보 입력
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트유저","email":"test@example.com"}'
```

#### 방법 B: Postman 사용

**컬렉션 설정:**
1. Base URL: `http://localhost:8080`
2. 각 API를 순서대로 테스트

**테스트 시나리오:**

**시나리오 1: 신규 사용자 첫 로그인**
1. `/auth/kakao/callback?code={카카오_인가코드}` (GET)
   - 예상 응답: `{"firstLogin": true, "message": "추가 정보 입력 필요"}`
2. `/api/user/register` (POST)
   - Body: `{"name":"홍길동","email":"hong@example.com"}`
   - 예상 응답: `"등록이 완료되었습니다."`
3. `/api/user/me` (GET)
   - 예상 응답: 사용자 정보 JSON

**시나리오 2: 기존 사용자 로그인**
1. `/auth/kakao/callback?code={카카오_인가코드}` (GET)
   - 예상 응답: `{"firstLogin": false, "message": "로그인 성공", "user": {...}}`
2. `/api/user/me` (GET)
   - 예상 응답: 사용자 정보 JSON

**시나리오 3: 로그아웃**
1. `/api/user/logout` (POST)
   - 예상 응답: `"로그아웃되었습니다."`
2. `/api/user/me` (GET)
   - 예상 응답: 401 Unauthorized, `"로그인이 필요합니다."`

### 3단계: 실제 카카오 로그인 플로우 테스트

**사전 준비:**
1. 카카오 개발자 콘솔에서 앱 등록
2. Redirect URI 설정: `http://localhost:8080/auth/kakao/callback`
3. `application-secret.yml`에 Client ID, Secret 설정

**테스트 절차:**
1. 브라우저에서 카카오 로그인 URL 접속:
   ```
   https://kauth.kakao.com/oauth/authorize?client_id={YOUR_CLIENT_ID}&redirect_uri=http://localhost:8080/auth/kakao/callback&response_type=code
   ```
2. 카카오 계정으로 로그인
3. 동의 화면에서 권한 승인
4. 콜백 URL로 리다이렉트되면서 로그인 처리

### 4단계: 데이터베이스 확인

```bash
# MySQL 접속
docker exec -it kernelLetter-mysql mysql -u root -p

# 비밀번호: 12345678

# 데이터베이스 선택
USE kernelLetter;

# 사용자 확인
SELECT * FROM user;

# 특정 사용자 상세 조회
SELECT kakao_id, name, email, is_first_login FROM user WHERE kakao_id = '123456789';
```

## ⚠️ 주의사항

1. **Session 유지**: curl로 테스트 시 세션 쿠키 관리 필요
2. **CSRF 토큰**: 프로덕션 환경에서는 CSRF 토큰 처리 필요
3. **카카오 API 제한**: 카카오 API Rate Limit 고려
4. **SSL/TLS**: 실제 배포 시 HTTPS 필수

## 📊 테스트 체크리스트

- [ ] 애플리케이션 정상 기동
- [ ] MySQL 연결 확인
- [ ] 신규 사용자 첫 로그인 시나리오
- [ ] 기존 사용자 로그인 시나리오
- [ ] 추가 정보 입력 기능
- [ ] 로그아웃 기능
- [ ] 현재 사용자 조회
- [ ] 로그인 없이 API 호출 시 401 에러
- [ ] DB에 사용자 정보 저장 확인

## 🐛 문제 해결

**문제 1: 포트 충돌**
```bash
# 8080 포트 사용 중인 프로세스 확인 및 종료
lsof -i :8080
kill -9 {PID}
```

**문제 2: MySQL 연결 실패**
```bash
# MySQL 컨테이너 재시작
docker-compose restart mysql

# 로그 확인
docker logs kernelLetter-mysql
```

**문제 3: 카카오 API 에러**
- `application-secret.yml`에서 Client ID/Secret 확인
- Redirect URI가 카카오 콘솔 설정과 일치하는지 확인

## 📝 테스트 결과 기록

```
테스트 일시: _______________
테스트자: _______________

[  ] 신규 사용자 첫 로그인 - 성공/실패
[  ] 기존 사용자 로그인 - 성공/실패
[  ] 추가 정보 입력 - 성공/실패
[  ] 로그아웃 - 성공/실패
[  ] 현재 사용자 조회 - 성공/실패

특이사항:
_____________________________________
_____________________________________
```

## 🔗 관련 파일

- Controller: `src/main/java/com/kernelLetter/controller/Kakao/KakaoAuthController.java`
- Service: `src/main/java/com/kernelLetter/service/KakaoAuthService.java`
- Tests: `src/test/java/com/kernelLetter/service/KakaoAuthServiceTest.java`
- 설정: `src/main/resources/application.yml`, `application-secret.yml`