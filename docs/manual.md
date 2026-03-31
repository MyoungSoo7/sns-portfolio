# SNS-Portfolio 사용자 매뉴얼

## 목차

1. [설치 방법](#설치-방법)
2. [실행 방법](#실행-방법)
3. [사용 가이드](#사용-가이드)
4. [주요 설정](#주요-설정)
5. [FAQ](#faq)

---

## 설치 방법

### 사전 요구사항

| 항목 | 버전 | 비고 |
|------|------|------|
| Java JDK | 11 | OpenJDK 또는 Oracle JDK |
| PostgreSQL | 13 이상 | 메인 데이터베이스 |
| Redis | 6.x 이상 | 캐시 및 세션 |
| Kafka | 3.x (선택) | 실시간 이벤트 처리 |
| Git | 최신 | |

### 프로젝트 클론

```bash
git clone <repository-url>
cd sns-portfolio
```

### 의존성 설치

```bash
./gradlew build
```

> React 프론트엔드는 Gradle 빌드에 통합되어 있어 별도의 npm 설치가 필요하지 않습니다.

### 데이터베이스 설정

PostgreSQL에서 데이터베이스를 생성합니다:

```sql
CREATE DATABASE sns_portfolio;
CREATE USER sns_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE sns_portfolio TO sns_user;
```

---

## 실행 방법

### 애플리케이션 실행

```bash
./gradlew bootRun
```

서버는 **포트 8087**에서 실행됩니다. React 프론트엔드는 Gradle 빌드 과정에서 함께 빌드되어 Spring Boot에 내장됩니다.

### 실행 확인

브라우저에서 `http://localhost:8087`에 접속합니다.

---

## 사용 가이드

### 기본 사용 흐름

1. **회원가입**: 메인 페이지에서 이메일과 비밀번호로 회원가입
2. **로그인**: 등록한 계정으로 로그인
3. **게시글 작성**: 텍스트, 이미지를 포함한 게시글 작성
4. **댓글/좋아요**: 다른 사용자의 게시글에 댓글 작성 및 좋아요
5. **실시간 알림**: 댓글, 좋아요 등 활동에 대한 실시간 알림 수신

### 주요 기능

| 기능 | 설명 |
|------|------|
| 게시글 CRUD | 게시글 작성, 조회, 수정, 삭제 |
| 댓글 | 게시글에 댓글 작성 및 관리 |
| 좋아요 | 게시글 좋아요/취소 |
| 실시간 알림 | Kafka 기반 실시간 이벤트 알림 (Kafka 미사용 시 동기 처리) |
| 사용자 프로필 | 프로필 조회 및 수정 |

---

## 주요 설정

### application.yaml

```yaml
server:
  port: 8087

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sns_portfolio
    username: sns_user
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

  data:
    redis:
      host: localhost
      port: 6379

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: sns-group

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24시간 (밀리초)
```

### 설정 항목 설명

| 항목 | 설명 |
|------|------|
| `spring.datasource.*` | PostgreSQL 접속 정보 |
| `spring.data.redis.*` | Redis 접속 정보 |
| `spring.kafka.*` | Kafka 브로커 접속 정보 (선택) |
| `jwt.secret` | JWT 토큰 서명 시크릿 키 |
| `jwt.expiration` | JWT 토큰 만료 시간 (밀리초) |

---

## FAQ

### Q: Kafka 없이도 실행할 수 있나요?

**A:** 네, Kafka는 선택 사항입니다. Kafka가 설치되어 있지 않거나 연결할 수 없는 경우, 실시간 이벤트 처리가 동기 방식으로 전환됩니다. 기본적인 SNS 기능(게시글, 댓글, 좋아요)은 Kafka 없이도 정상 동작합니다.

Kafka를 비활성화하려면 `application.yaml`에서 Kafka 관련 설정을 제거하거나 프로파일을 분리하여 사용하세요.

### Q: Redis 연결에 실패합니다.

**A:** 다음을 확인하세요:

1. Redis 서버가 실행 중인지 확인:
   ```bash
   redis-cli ping
   # 정상 응답: PONG
   ```
2. Redis가 설치되어 있지 않다면:
   ```bash
   # macOS
   brew install redis
   brew services start redis

   # Ubuntu/Debian
   sudo apt install redis-server
   sudo systemctl start redis
   ```
3. `application.yaml`의 Redis 호스트와 포트가 올바른지 확인

### Q: 프론트엔드 빌드에서 에러가 발생합니다.

**A:** React 프론트엔드는 Gradle 빌드에 통합되어 있습니다. 빌드 에러 발생 시 다음을 확인하세요:

1. **Node.js 버전 확인**: Gradle의 Node 플러그인이 자동으로 Node.js를 관리하지만, 시스템에 설치된 Node.js와 충돌할 수 있습니다.
2. **캐시 초기화 후 재빌드**:
   ```bash
   ./gradlew clean build
   ```
3. **Node 모듈 초기화**:
   ```bash
   rm -rf frontend/node_modules
   ./gradlew build
   ```
4. Gradle 빌드 로그에서 구체적인 에러 메시지를 확인하여 원인을 파악하세요.
