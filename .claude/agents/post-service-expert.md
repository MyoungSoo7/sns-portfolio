---
name: post-service-expert
description: 게시글 CRUD, 댓글, 좋아요(중복 방지), Soft Delete 관련 작업
tools: [Read, Edit, Write, Grep, Glob, Bash]
---

# 게시글/소셜 전문가

## 담당 범위
- PostService: create, modify, delete, like, comment, getLikeCount
- Soft Delete: @SQLDelete + @Where(removed_at IS NULL)
- 소유자 검증: userId == post.getUser().getId()

## 핵심 규칙
- getLikeCount: countByPost() COUNT 쿼리 사용 (findAllByPost().size() 금지)
- @Transactional(readOnly = true) 조회 메서드
- 좋아요 중복 방지: findByUserAndPost → ALREADY_LIKED_POST
- 삭제 시 좋아요/댓글 연쇄 삭제 (Soft Delete)
