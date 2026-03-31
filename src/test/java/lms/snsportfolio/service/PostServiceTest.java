package lms.snsportfolio.service;

import lms.snsportfolio.exception.SimpleSnsApplicationException;
import lms.snsportfolio.model.entity.PostEntity;
import lms.snsportfolio.model.entity.UserEntity;
import lms.snsportfolio.producer.AlarmProducer;
import lms.snsportfolio.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock UserEntityRepository userEntityRepository;
    @Mock PostEntityRepository postEntityRepository;
    @Mock CommentEntityRepository commentEntityRepository;
    @Mock LikeEntityRepository likeEntityRepository;
    @Mock AlarmProducer alarmProducer;
    @InjectMocks PostService postService;

    @Test @DisplayName("포스트 생성 — 사용자 미존재")
    void create_userNotFound() {
        when(userEntityRepository.findByUserName("none")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create("none", "title", "body"))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }

    @Test @DisplayName("포스트 수정 — 권한 없음")
    void modify_noPermission() {
        PostEntity post = mock(PostEntity.class);
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(2);
        when(post.getUser()).thenReturn(user);

        when(postEntityRepository.findById(1)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.modify(999, 1, "new", "body"))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }

    @Test @DisplayName("포스트 삭제 — 미존재")
    void delete_notFound() {
        when(postEntityRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(1, 999))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }

    @Test @DisplayName("좋아요 — 이미 좋아요한 경우")
    void like_alreadyLiked() {
        PostEntity post = mock(PostEntity.class);
        UserEntity user = mock(UserEntity.class);

        when(postEntityRepository.findById(1)).thenReturn(Optional.of(post));
        when(userEntityRepository.findByUserName("user1")).thenReturn(Optional.of(user));
        when(likeEntityRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(mock(lms.snsportfolio.model.entity.LikeEntity.class)));

        assertThatThrownBy(() -> postService.like(1, "user1"))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }

    @Test @DisplayName("getLikeCount — COUNT 쿼리 사용")
    void getLikeCount() {
        PostEntity post = mock(PostEntity.class);
        when(postEntityRepository.findById(1)).thenReturn(Optional.of(post));
        when(likeEntityRepository.countByPost(post)).thenReturn(5);

        Integer count = postService.getLikeCount(1);

        assertThat(count).isEqualTo(5);
        verify(likeEntityRepository).countByPost(post);
        verify(likeEntityRepository, never()).findAllByPost(any());
    }
}
