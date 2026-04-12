# STATUS — SNS Portfolio

> 게시글·댓글·좋아요 + Kafka/SSE 기반 실시간 알림 SNS 백엔드

**Last updated:** 2026-04-09

## 현재 상태
- **활성 브랜치:** `master`
- **스택:** Spring Boot 3.2.5 / Java 17 / Kafka / Redis / SSE
- **최근 커밋:** `96b212f` docs: add Swagger/OpenAPI documentation

## 최근 진척
- **Spring Boot 2.6 → 3.2.5 메이저 업그레이드** (javax → jakarta, SecurityFilterChain 현대화)
- Swagger/OpenAPI 문서화
- Claude 에이전트 3개 추가 (게시글/실시간알림/Boot3 마이그레이션)
- `DevController @Profile("dev")` 제한, Kafka `@Configuration` 활성화
- ai-dev-team 커맨드 + 17개 에이전트 프롬프트
- README 전면 개편 (Boot 3.2.5 반영)

## 진행 중
- 마이그레이션 후 회귀 테스트 보강
- 실시간 알림 SSE 재연결 시나리오 정리

## 다음 할 일
- [ ] Kafka 프로듀서/컨슈머 idempotency 검증
- [ ] 알림 모듈 부하 테스트
- [ ] 인증/인가 경계 문서화

## 주요 위험/메모
- Boot 3 이전 시 javax/jakarta 혼재가 과거 이슈 — 누락된 의존성 주기적 점검
- SSE 연결 누수 모니터링 필요

## 참고 문서
- `README.md` — 프로젝트 개요 & 아키텍처
- `CLAUDE.md` — 에이전트 운용 가이드
- `HARNESS.md` — Claude Code 개발 하네스 구성
