package lms.snsportfolio.service;

import lms.snsportfolio.exception.ErrorCode;
import lms.snsportfolio.exception.SimpleSnsApplicationException;
import lms.snsportfolio.model.User;
import lms.snsportfolio.model.entity.UserEntity;
import lms.snsportfolio.repository.AlarmEntityRepository;
import lms.snsportfolio.repository.UserCacheRepository;
import lms.snsportfolio.repository.UserEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserEntityRepository userRepository;
    @Mock AlarmEntityRepository alarmEntityRepository;
    @Mock BCryptPasswordEncoder encoder;
    @Mock UserCacheRepository redisRepository;
    @InjectMocks UserService userService;

    @Test @DisplayName("loadUserByUsername — Redis 캐시 히트")
    void loadUser_cacheHit() {
        User cached = mock(User.class);
        when(redisRepository.getUser("testuser")).thenReturn(Optional.of(cached));

        User result = userService.loadUserByUsername("testuser");

        assertThat(result).isSameAs(cached);
        verify(userRepository, never()).findByUserName(any());
    }

    @Test @DisplayName("loadUserByUsername — 캐시 미스 → DB 조회")
    void loadUser_cacheMiss_dbHit() {
        UserEntity entity = mock(UserEntity.class);
        when(entity.getId()).thenReturn(1);
        when(entity.getUserName()).thenReturn("testuser");
        when(entity.getPassword()).thenReturn("encoded");

        when(redisRepository.getUser("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(entity));

        User result = userService.loadUserByUsername("testuser");

        assertThat(result).isNotNull();
    }

    @Test @DisplayName("loadUserByUsername — 사용자 미존재")
    void loadUser_notFound() {
        when(redisRepository.getUser("none")).thenReturn(Optional.empty());
        when(userRepository.findByUserName("none")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("none"))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }

    @Test @DisplayName("login — 비밀번호 불일치")
    void login_wrongPassword() {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn("encoded");
        when(redisRepository.getUser("user1")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded")).thenReturn(false);

        ReflectionTestUtils.setField(userService, "secretKey", "test-secret-key-12345");
        ReflectionTestUtils.setField(userService, "expiredTimeMs", 3600000L);

        assertThatThrownBy(() -> userService.login("user1", "wrong"))
                .isInstanceOf(SimpleSnsApplicationException.class);
    }
}
