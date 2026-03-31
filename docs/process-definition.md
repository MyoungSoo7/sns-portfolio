# 프로세스정의서 — SNS 포트폴리오

## 1. 게시글 CRUD 프로세스

### 1.1 프로세스 개요

| 항목 | 내용 |
|------|------|
| 프로세스명 | 게시글 CRUD 프로세스 |
| 트리거 | 사용자의 게시글 작성/조회/수정/삭제 요청 |
| 입력 | 제목, 본문 (작성/수정), 게시글 ID (조회/수정/삭제) |
| 출력 | 게시글 생성/수정/삭제 결과 |
| 관련 API | `/api/v1/posts` |

### 1.2 프로세스 흐름도

```mermaid
flowchart TD
    subgraph 작성_Create
        A1[사용자: 제목/본문 입력] --> B1["POST /api/v1/posts"]
        B1 --> C1[JWT 토큰에서 사용자 추출]
        C1 --> D1[PostEntity 생성 및 저장]
        D1 --> E1[성공 응답 반환]
    end

    subgraph 조회_Read
        A2["GET /api/v1/posts"] --> B2[Pageable로 전체 목록 조회]
        B2 --> C2[PostEntity → PostDto 변환]
        C2 --> D2[페이지네이션 응답]
    end

    subgraph 수정_Update
        A3["사용자: 수정 요청"] --> B3["PUT /api/v1/posts/{id}"]
        B3 --> C3[게시글 존재 확인]
        C3 --> D3{소유자 검증}
        D3 -->|불일치| E3[403 Forbidden - 본인 글만 수정 가능]
        D3 -->|일치| F3[제목/본문 업데이트]
        F3 --> G3[성공 응답]
    end

    subgraph 삭제_Delete
        A4["사용자: 삭제 요청"] --> B4["DELETE /api/v1/posts/{id}"]
        B4 --> C4[게시글 존재 확인]
        C4 --> D4{소유자 검증}
        D4 -->|불일치| E4[403 Forbidden]
        D4 -->|일치| F4[Soft Delete 처리]
        F4 --> G4[연관 좋아요 Soft Delete]
        G4 --> H4[연관 댓글 Soft Delete]
        H4 --> I4[연관 알림 Soft Delete]
        I4 --> J4[성공 응답]
    end
```

### 1.3 상세 처리 규칙

| 단계 | 처리 내용 | 비고 |
|------|-----------|------|
| 인증 | JWT 토큰에서 userName 추출 후 UserEntity 조회 | `@AuthenticationPrincipal` 사용 |
| 소유자 검증 | `post.getUser().equals(currentUser)` | 불일치 시 403 반환 |
| Soft Delete | `deletedAt` 필드에 현재 시간 설정 | 물리 삭제 아님 |
| 연쇄 삭제 | 좋아요 → 댓글 → 알림 순서로 Soft Delete | 데이터 정합성 유지 |
| 페이지네이션 | `Pageable(page, size, sort=createdAt,desc)` | 기본 20건 |

---

## 2. 좋아요 + 알림 프로세스

### 2.1 프로세스 개요

| 항목 | 내용 |
|------|------|
| 프로세스명 | 좋아요 + 알림 프로세스 |
| 트리거 | 사용자가 게시글의 좋아요 버튼 클릭 |
| 입력 | 게시글 ID |
| 출력 | LikeEntity 저장 + 글 작성자에게 실시간 알림 |
| 관련 기술 | Kafka, SSE |

### 2.2 프로세스 흐름도

```mermaid
flowchart TD
    A[사용자: 좋아요 클릭] --> B["POST /api/v1/posts/{postId}/likes"]
    B --> C[JWT에서 사용자 추출]
    C --> D[게시글 존재 확인]
    D --> E{중복 좋아요 체크}
    E -->|이미 좋아요 함| F[409 Conflict - 중복 좋아요 불가]
    E -->|신규 좋아요| G[LikeEntity 생성 및 저장]
    G --> H{본인 글에 좋아요?}
    H -->|예| I[알림 발행 안 함]
    H -->|아니오| J[Kafka AlarmEvent 발행]

    J --> K[Kafka Topic: alarm]
    K --> L[AlarmConsumer 수신]
    L --> M[AlarmEntity 생성 및 DB 저장]
    M --> N[EmitterRepository에서 글 작성자 emitter 조회]
    N --> O{emitter 존재?}
    O -->|아니오| P[SSE 미전송 - DB에만 저장]
    O -->|예| Q["emitter.send(알림 데이터)"]
    Q --> R[브라우저에 실시간 알림 표시]
```

### 2.3 상세 처리 규칙

| 단계 | 처리 내용 | 비고 |
|------|-----------|------|
| 중복 체크 | `LikeRepository.findByUserAndPost(user, post)` | 존재 시 409 반환 |
| LikeEntity | user, post 연관관계 | `@ManyToOne` |
| Kafka 발행 | Topic: `alarm`, Key: postId, Value: AlarmEvent(NEW_LIKE, targetUserId, fromUserId, postId) | 비동기 처리 |
| AlarmEntity | alarmType=NEW_LIKE, user(글 작성자), args(좋아요 누른 사용자, 게시글 ID) | DB 영구 저장 |
| SSE 전송 | `SseEmitter.send(SseEventBuilder)` | 실시간 푸시 |

### 2.4 Kafka 이벤트 흐름

```mermaid
flowchart LR
    subgraph Producer_API_서버
        A[LikeService] -->|AlarmEvent| B[KafkaProducer]
    end

    subgraph Kafka_Broker
        B --> C[Topic: alarm]
    end

    subgraph Consumer_알림_처리
        C --> D[AlarmConsumer]
        D --> E[AlarmEntity 저장]
        D --> F[SSE 전송]
    end
```

---

## 3. 댓글 + 알림 프로세스

### 3.1 프로세스 개요

| 항목 | 내용 |
|------|------|
| 프로세스명 | 댓글 + 알림 프로세스 |
| 트리거 | 사용자가 게시글에 댓글 작성 |
| 입력 | 게시글 ID, 댓글 내용 |
| 출력 | CommentEntity 저장 + 글 작성자에게 실시간 알림 |
| 관련 기술 | Kafka, SSE |

### 3.2 프로세스 흐름도

```mermaid
flowchart TD
    A[사용자: 댓글 작성] --> B["POST /api/v1/posts/{postId}/comments"]
    B --> C[JWT에서 사용자 추출]
    C --> D[게시글 존재 확인]
    D --> E[CommentEntity 생성 및 저장]
    E --> F{본인 글에 댓글?}
    F -->|예| G[알림 발행 안 함]
    F -->|아니오| H["Kafka AlarmEvent 발행 (NEW_COMMENT)"]
    H --> I[Kafka Topic: alarm]
    I --> J[AlarmConsumer 수신]
    J --> K[AlarmEntity 생성 및 DB 저장]
    K --> L[EmitterRepository에서 글 작성자 emitter 조회]
    L --> M{emitter 존재?}
    M -->|아니오| N[SSE 미전송]
    M -->|예| O["emitter.send(NEW_COMMENT 알림)"]
    O --> P[브라우저에 실시간 댓글 알림 표시]
```

### 3.3 상세 처리 규칙

| 단계 | 처리 내용 | 비고 |
|------|-----------|------|
| CommentEntity | user(작성자), post(게시글), comment(내용) | `@ManyToOne` 양방향 |
| Kafka 발행 | AlarmEvent(NEW_COMMENT, targetUserId, fromUserId, postId) | 좋아요와 동일 Topic 사용 |
| 알림 메시지 | "{사용자명}님이 댓글을 남겼습니다" | alarmType으로 메시지 포맷 결정 |
| 자기 댓글 | 본인 글에 댓글 시 알림 미발행 | 불필요한 알림 방지 |

---

## 4. SSE 구독 프로세스

### 4.1 프로세스 개요

| 항목 | 내용 |
|------|------|
| 프로세스명 | SSE(Server-Sent Events) 구독 프로세스 |
| 트리거 | 사용자가 서비스 접속 (프론트엔드 EventSource 연결) |
| 입력 | 인증된 사용자 정보 |
| 출력 | 실시간 알림 스트림 |
| 관련 기술 | SseEmitter, ConcurrentHashMap |

### 4.2 프로세스 흐름도

```mermaid
flowchart TD
    A["브라우저: GET /api/v1/alarm/subscribe"] --> B[JWT 인증 확인]
    B --> C[SseEmitter 생성 - timeout 60분]
    C --> D[EmitterRepository에 저장]
    D --> E["ConcurrentHashMap<userId, SseEmitter>"]
    E --> F[초기 연결 이벤트 전송 - 연결 확인용]
    F --> G[SSE 연결 유지]

    G --> H{이벤트 발생?}
    H -->|좋아요 알림| I["emitter.send(NEW_LIKE 데이터)"]
    H -->|댓글 알림| J["emitter.send(NEW_COMMENT 데이터)"]
    I --> G
    J --> G

    G --> K{연결 만료?}
    K -->|60분 경과| L[emitter.complete 호출]
    L --> M[ConcurrentHashMap에서 제거]
    M --> N[브라우저 EventSource 자동 재연결]
    N --> A
```

### 4.3 상세 처리 규칙

| 단계 | 처리 내용 | 비고 |
|------|-----------|------|
| SseEmitter 생성 | `new SseEmitter(3600000L)` — 60분 타임아웃 | Spring MVC 기본 제공 |
| 저장소 | `ConcurrentHashMap<String, SseEmitter>` | 스레드 안전, 인메모리 |
| 초기 이벤트 | 연결 직후 더미 이벤트 전송 | 503 에러 방지 (빈 스트림 방지) |
| 이벤트 전송 | `emitter.send(SseEmitter.event().name("alarm").data(alarmData))` | JSON 형식 |
| 만료 처리 | `onCompletion`, `onTimeout` 콜백에서 Map 제거 | 메모리 누수 방지 |
| 에러 처리 | 전송 실패 시 해당 emitter 제거 | 클라이언트 이탈 대응 |

### 4.4 SSE 상태 다이어그램

```mermaid
stateDiagram-v2
    [*] --> 연결요청: GET /alarm/subscribe
    연결요청 --> 연결수립: SseEmitter 생성
    연결수립 --> 대기중: 초기 이벤트 전송
    대기중 --> 이벤트전송: 알림 발생
    이벤트전송 --> 대기중: 전송 완료
    대기중 --> 연결종료: 60분 타임아웃
    대기중 --> 연결종료: 클라이언트 이탈
    이벤트전송 --> 연결종료: 전송 실패
    연결종료 --> 정리: emitter 제거
    정리 --> [*]: 재연결 대기
    정리 --> 연결요청: EventSource 자동 재연결
```

### 4.5 전체 알림 아키텍처

```mermaid
flowchart TD
    subgraph 이벤트_발생
        LIKE[좋아요 클릭]
        COMMENT[댓글 작성]
    end

    subgraph Kafka
        LIKE --> PRODUCER[KafkaProducer]
        COMMENT --> PRODUCER
        PRODUCER --> TOPIC[Topic: alarm]
        TOPIC --> CONSUMER[AlarmConsumer]
    end

    subgraph 알림_처리
        CONSUMER --> DB_SAVE[AlarmEntity DB 저장]
        CONSUMER --> SSE_SEND[SSE 실시간 전송]
    end

    subgraph SSE_인프라
        SSE_SEND --> EMITTER_REPO["EmitterRepository (ConcurrentHashMap)"]
        EMITTER_REPO --> EMITTER["SseEmitter.send()"]
        EMITTER --> BROWSER[브라우저 실시간 수신]
    end

    subgraph 조회
        BROWSER --> ALARM_PAGE["알림 페이지 GET /api/v1/alarm"]
        ALARM_PAGE --> DB_SAVE
    end
```
