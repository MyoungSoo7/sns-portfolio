package lms.snsportfolio.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserRoleTest {
    @Test void values() {
        assertThat(UserRole.values()).containsExactlyInAnyOrder(UserRole.USER, UserRole.ADMIN);
    }
}
