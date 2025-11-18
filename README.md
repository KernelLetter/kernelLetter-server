# kernelLetter-server

## 개발 가이드 
- 각각의 레이어 계층에 본인이 담당한 기능의 패키지를 생성 후 그 안에 코드 파일 생성
  - ex) controller/user/UserController.java

**데이터 베이스는 Docker환경입니다. 서버 실행 전** 터미널에서 아래 명령어 실행

도커 실행 명령어

`docker-compose up -d`

도커 중지 명령어

`docker-compose down`

---

## Global Exception 처리

프로젝트는 전역 예외 처리 시스템을 사용합니다. 각 레이어에 맞는 Exception을 사용하세요.

### 예외 클래스 사용 규칙

- **Service 레이어**: `BusinessException` 사용
- **Controller 레이어**: `ControllerException` 사용

### ErrorCode 추가 방법

새로운 에러가 필요한 경우 `ErrorCode.java`에 상황에 맞는 코드를 추가하세요.

```java
// ErrorCode.java 예시
// 게시글 관련
POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P-001", "게시글을 찾을 수 없습니다."),
POST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "P-002", "이미 삭제된 게시글입니다."),

// 댓글 관련
COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C-001", "댓글을 찾을 수 없습니다."),
```

### Service 레이어 사용 예시

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));
    }

    public void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS__EMAIL);
        }
    }
}
```

---

# 컨벤션

### 1-1. **네이밍 규칙 (Naming Rules)**

- **변수 및 함수 이름**: CamelCase 사용 (ex: `getUserData`, `userInfo`)
- **클래스 이름**: PascalCase 사용 (ex: `UserProfile`, `ProductManager`)
- **상수**: UPPER_SNAKE_CASE 사용 (ex: `MAX_RETRY_COUNT`)0
- **파일 이름**: 소문자 및 hyphen(-) 사용 (ex: `user-controller.js`, `app-config.ts`)

### 1-2. **들여쓰기 및 공백 (Indentation & Spacing)**

- **탭 크기**: Tabs
- **라인 길이**: 80자 제한 권장
- **함수 간 공백**: 함수와 함수 사이 한 줄 공백 유지
- **중괄호 위치**: 중괄호는 한 줄 아래
- (ex:

  `if (condition) {`

             `...`    

  `}`            )


### 1-3. **주석 규칙 (Commenting Rules)**

- **함수 주석**: 함수 상단에 해당 함수의 목적, 입력값, 반환값 명시
- **코드 설명 주석**: 코드가 복잡하거나 중요한 부분에 설명 추가
- 단, 무분별한 주석은 제한

### 1-4. **코드 구조 (Code Structure)**

- **모듈화**: 관련 기능별로 코드를 모듈화 (ex: services, controllers, utils 등)
- **파일 구조 통일**: be/fe


    ### 1-5. **Git 전략 & 커밋 컨벤션**

    - **브랜치 네이밍**

        ```
        main       → 운영 배포용
        develop    → 개발 통합
        feature/*  → 기능 단위
        fix/*      → 버그 수정
        hotfix/*   → 긴급 수정
        refactor/* -> 코드 리팩토링
        ```

    - **커밋 메시지**
        ```
        feat: 새로운 기능 추가
        fix: 버그 수정
        docs: 문서 수정
        style: 코드 포맷팅, 세미콜론 누락 등
        refactor: 코드 리팩토링
        test: 테스트 코드 추가
        chore: 빌드 업무 수정, 패키지 매니저 수정
        ```

        예시: `feat: 사용자 로그인 기능 구현`

---

## 2. 프로젝트 구조

```
src/main/java/com/kernelLetter/
├── controller/     # REST API 엔드포인트 (요청/응답 처리)
├── service/        # 비즈니스 로직
├── repository/     # 데이터베이스 접근 계층
├── domain/         # 엔티티 클래스
├── dto/            # 데이터 전송 객체
└── config/         # 설정 클래스
```

---

## 3. 로컬 개발 환경 요구사항

- **Java 21** 이상
- **Docker Desktop** (데이터베이스용)
- **Gradle** (Wrapper 포함)
- **IDE**: IntelliJ IDEA 권장

---

## 4. PR/코드 리뷰 프로세스

1. feature 브랜치에서 작업
2. 작업 완료 후 develop 브랜치로 PR 생성
3. 최소 1명 이상의 리뷰어 승인 필요
4. 모든 CI 테스트 통과 확인
5. Squash and Merge로 병합


