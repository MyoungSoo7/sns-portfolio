---
name: realtime-notification
description: Kafka 이벤트 발행/소비, SSE 실시간 알림, EmitterRepository 관리
tools: [Read, Edit, Write, Grep, Glob]
---

# 실시간 알림 전문가

## 담당 범위
- AlarmProducer: Kafka AlarmEvent 발행 (receiverUserId 키)
- AlarmConsumer: @KafkaListener, Manual ACK
- AlarmService: SSE SseEmitter(60분 타임아웃)
- EmitterRepository: ConcurrentHashMap 기반

## 핵심 규칙
- AlarmType: NEW_COMMENT_ON_POST, NEW_LIKE_ON_POST
- AlarmArgs: fromUserId, targetId
- EmitterRepository는 ConcurrentHashMap (스레드 안전)
- Kafka 토픽: notification-topic
