# SNS 포트폴리오 (소셜 네트워크 서비스)

## 프로젝트 개요

게시글, 댓글, 좋아요, 실시간 알림 기능을 갖춘 소셜 네트워크 서비스. Kafka와 SSE를 활용한 실시간 이벤트 처리 지원.

## 기술 스택

- **언어**: Java 11
- **프레임워크**: Spring Boot 2.6.7
- **데이터베이스**: PostgreSQL
- **캐시**: Redis (Lettuce)
- **메시지 브로커**: Kafka
- **인증**: JWT (jjwt 0.11.5)
- **프론트엔드**: React (Gradle 통합 빌드)

## 주요 기능

- 게시글 CRUD (작성, 조회, 수정, 삭제)
- 댓글 기능
- 좋아요 (중복 방지)
- 실시간 알림 (SSE + Kafka)
- 회원가입 및 JWT 기반 로그인

## 패키지 구조

```
├── controller        # REST 컨트롤러
├── service           # 비즈니스 로직
├── model/
│   ├── entity        # JPA 엔티티
│   └── domain        # 도메인 객체
├── repository/       # 데이터 접근
│   ├── JPA           # JPA 리포지토리
│   └── Redis cache   # Redis 캐시 리포지토리
├── configuration     # 설정 (Security, Kafka, Redis 등)
├── producer          # Kafka 프로듀서
├── consumer          # Kafka 컨슈머
├── exception         # 예외 처리
└── utils             # 유틸리티 (JWT 등)
```

## 보안

- **JWT**: 만료기간 30일
- **비밀번호**: BCrypt 암호화
- **세션**: Stateless Session 정책
- **인증 실패**: 커스텀 AuthenticationEntryPoint

## 이벤트 처리

- **Kafka**: 댓글, 좋아요 발생 시 알림 이벤트 발행
- **SSE (Server-Sent Events)**: 실시간 알림 푸시
- **EmitterRepository**: ConcurrentHashMap 기반 SSE Emitter 관리

## Soft Delete

- `@SQLDelete` + `@Where(clause = "removed_at IS NULL")`
- 삭제 시 `removed_at` 타임스탬프 기록, 조회 시 자동 필터링

## 성능 최적화

- HikariCP 커넥션 풀: 15
- `batch_fetch_size`: 30
- `open-in-view`: false
- GZip 압축 활성화
- DB 인덱스: post, comment, like 테이블
- `getLikeCount`: COUNT 쿼리 최적화
