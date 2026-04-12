# HARNESS — SNS Portfolio

> Claude Code 개발 하네스 구성 — Boot 3 마이그레이션 + 실시간 알림 전용 에이전트

**Last updated:** 2026-04-09

## 목적
SNS 포트폴리오는 **Spring Boot 2.6 → 3.2.5 메이저 업그레이드**를 완료한 직후다.
마이그레이션 잔여 이슈·실시간 알림 파이프라인(Kafka+SSE)처럼 도메인 특성이 뚜렷한 작업은 전용 에이전트로 분리해 운영한다.

## 디렉토리 구조
```
.claude/
├── agents/
│   ├── post-service-expert.md       # 게시글/댓글/좋아요 도메인
│   ├── realtime-notification.md     # Kafka + SSE 실시간 알림 전문
│   └── spring-boot3-migration.md    # javax → jakarta 마이그레이션
└── commands/
    ├── agents/
    └── ai-dev-team.md
```

## 에이전트 사용 원칙
1. **게시글 도메인 변경** → `post-service-expert`
2. **알림 로직 수정** → `realtime-notification` (SSE 재연결·Kafka idempotency 체크리스트 포함)
3. **Boot 2 → 3 잔여 마이그레이션 작업** → `spring-boot3-migration` (javax/jakarta 변환 패턴 숙지)

## 커맨드
- `/ai-dev-team` — 역할 기반 산출물 일괄 생성

## 확장 가이드
- Boot 3 전환이 완료되면 `spring-boot3-migration` 에이전트는 **아카이브**로 이동 가능
- 실시간 알림 에이전트는 부하 테스트 시나리오와 함께 갱신할 것

## 관련 문서
- `CLAUDE.md` — 에이전트 운용 규칙
- `STATUS.md` — 마이그레이션/알림 현황
- `README.md` — 프로젝트 개요
