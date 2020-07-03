package net.syscon.prison.security;

import org.junit.Test;

import static net.syscon.prison.security.AuthSource.AUTH;
import static net.syscon.prison.security.AuthSource.NONE;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthSourceTest {

    @Test
    public void fromName_exists_returnsAuthSource() {
        assertThat(AuthSource.fromName("auth")).isEqualTo(AUTH);
    }

    @Test
    public void fromName_doesNotExist_defaultsToNone() {
        assertThat(AuthSource.fromName("not an auth source")).isEqualTo(NONE);
    }

    @Test
    public void fromName_null_defaultsToNone() {
        assertThat(AuthSource.fromName(null)).isEqualTo(NONE);
    }
}
