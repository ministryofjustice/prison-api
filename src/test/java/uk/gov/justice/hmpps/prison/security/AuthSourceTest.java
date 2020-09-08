package uk.gov.justice.hmpps.prison.security;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.security.AuthSource.AUTH;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NONE;

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
