package lms.snsportfolio.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

class JwtTokenUtilsTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac";
    private static final long EXPIRE_MS = 3600000L;

    @Test @DisplayName("토큰 생성 및 유효성 검증")
    void generateAndValidate() {
        String token = JwtTokenUtils.generateAccessToken("testuser", SECRET, EXPIRE_MS);
        assertThat(token).isNotBlank();

        Boolean valid = JwtTokenUtils.validate(token, "testuser", SECRET);
        assertThat(valid).isTrue();
    }

    @Test @DisplayName("잘못된 토큰 검증 실패")
    void invalidToken() {
        Boolean valid = JwtTokenUtils.validate("invalid.token.here", "user", SECRET);
        assertThat(valid).isFalse();
    }

    @Test @DisplayName("토큰에서 사용자명 추출")
    void getUserName() {
        String token = JwtTokenUtils.generateAccessToken("myuser", SECRET, EXPIRE_MS);
        String userName = JwtTokenUtils.getUserName(token, SECRET);
        assertThat(userName).isEqualTo("myuser");
    }
}
