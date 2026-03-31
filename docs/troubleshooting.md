# 트러블슈팅 가이드

## 1. Kafka 연결 실패

**증상:** `TimeoutException` 또는 `BrokerNotAvailableException` 발생

**해결 방법:**
- `bootstrap-servers` 설정이 **localhost:9092**로 올바르게 되어 있는지 확인
- Kafka 브로커가 실행 중인지 확인: `docker ps` 또는 `kafka-broker-api-versions.sh`
- 토픽 자동 생성(auto.create.topics.enable)이 활성화되어 있는지 확인
- Zookeeper가 정상 동작 중인지 확인

---

## 2. Redis 연결 실패

**증상:** `RedisConnectionFailureException` 발생, 사용자 캐시 조회 불가

**해결 방법:**
- Redis URL이 **redis://localhost:6379**로 올바르게 설정되어 있는지 확인
- Redis 서비스가 실행 중인지 확인: `redis-cli ping` → `PONG` 응답 확인
- Docker 환경이라면 `docker compose up -d redis` 실행

---

## 3. PostgreSQL 연결 실패

**증상:** 서버 시작 시 `PSQLException` 또는 연결 거부 에러 발생

**해결 방법:**
- `spring.datasource.url`, `username`, `password`가 올바르게 설정되어 있는지 확인
- PostgreSQL 서비스가 실행 중인지 확인
- 방화벽이나 보안 그룹에서 PostgreSQL 포트(기본 5432)가 열려 있는지 확인

---

## 4. SSE 알림 안 옴

**증상:** 좋아요/댓글 후 실시간 알림이 수신되지 않음

**해결 방법:**
- `EmitterRepository`가 **ConcurrentHashMap**으로 구현되어 있어, 서버 재시작 시 모든 emitter가 초기화됨
- 클라이언트가 `/alarm/subscribe`를 다시 호출하여 SSE 연결을 재수립해야 함
- SseEmitter 타임아웃이 60분이므로, 장시간 미접속 시 재연결 필요
- 브라우저 개발자 도구의 Network 탭에서 EventStream 연결 상태 확인

---

## 5. JWT 만료

**증상:** API 호출 시 401 Unauthorized 응답, 토큰 만료 메시지

**해결 방법:**
- JWT 유효기간이 **30일**로 설정되어 있음
- `jwt.secret-key` 환경변수가 올바르게 설정되어 있는지 확인
- 토큰 재발급을 위해 `/login` 엔드포인트를 다시 호출
- 서버 재시작 시 secret-key가 변경되면 기존 토큰이 모두 무효화됨

---

## 6. 좋아요 중복 에러

**증상:** 동일 게시글에 좋아요를 두 번 누를 때 409 Conflict 응답

**해결 방법:**
- 이는 **정상 동작**임 (ALREADY_LIKED_POST 에러 코드)
- 이미 좋아요를 누른 게시글에는 중복 좋아요가 방지됨
- 클라이언트에서 좋아요 상태를 확인하고 UI를 적절히 처리할 것

---

## 7. 의존성 취약점 86개

**증상:** GitHub Dependabot에서 보안 취약점 알림 86건 발생

**해결 방법:**
- GitHub 저장소의 Security 탭 > Dependabot alerts 확인
- Spring Boot 버전 업그레이드 권장 (최신 패치 버전으로 업데이트)
- `./gradlew dependencyUpdates` 또는 `mvn versions:display-dependency-updates`로 업데이트 가능한 라이브러리 확인
- 우선순위: Critical/High 취약점부터 순차적으로 해결

---

## 8. getLikeCount 성능

**증상:** 게시글 목록 조회 시 좋아요 수 계산으로 인한 성능 저하

**해결 방법:**
- 기존: `findAll().size()` -- 전체 LikeEntity를 메모리에 로딩하여 count
- 수정됨: `countByPost()` -- DB에서 **COUNT 쿼리**로 직접 집계
- 추가 성능 개선이 필요하면 좋아요 수를 Post 테이블에 비정규화하여 캐싱 검토
