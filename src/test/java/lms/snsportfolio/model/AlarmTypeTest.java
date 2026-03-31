package lms.snsportfolio.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AlarmTypeTest {
    @Test void values() {
        assertThat(AlarmType.values()).containsExactlyInAnyOrder(
                AlarmType.NEW_COMMENT_ON_POST, AlarmType.NEW_LIKE_ON_POST);
    }
}
