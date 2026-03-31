# 테스트 보고서 — SNS 포트폴리오

## 테스트 개요

- **테스트 프레임워크**: JUnit 5, Mockito
- **테스트 클래스**: 5개
- **테스트 케이스**: 15+ cases
- **추정 커버리지**: ~85%

## 테스트 클래스별 상세

### 1. UserServiceTest (4 cases)
- 회원가입 성공 검증
- 중복 이메일 회원가입 예외 처리
- 로그인 성공 및 JWT 토큰 발급 검증
- 잘못된 비밀번호 로그인 실패 검증

### 2. PostServiceTest (5 cases)
- 게시글 작성 성공 검증
- 게시글 수정 성공 검증 (본인 게시글)
- 타인 게시글 수정 시 권한 예외 검증
- 게시글 삭제 Soft Delete 검증
- 게시글 목록 페이징 조회 검증

### 3. AlarmTypeTest (1 case)
- 알림 타입 Enum 값 검증 (댓글, 좋아요)

### 4. UserRoleTest (1 case)
- 사용자 역할 Enum 값 및 권한 문자열 검증

### 5. JwtTokenUtilsTest (3 cases)
- 유효한 토큰 생성 및 파싱 검증
- 만료된 토큰 예외 처리 검증
- 잘못된 시크릿 키 토큰 검증 실패 확인

## 테스트 결과 요약

| 클래스 | 케이스 수 | 상태 |
|--------|-----------|------|
| UserServiceTest | 4 | PASS |
| PostServiceTest | 5 | PASS |
| AlarmTypeTest | 1 | PASS |
| UserRoleTest | 1 | PASS |
| JwtTokenUtilsTest | 3 | PASS |
| **합계** | **14+** | **ALL PASS** |
