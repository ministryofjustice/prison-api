package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderTrustAccountTest {
    @Test
    void isAccountClosed_NotSet() {
        assertThat(new OffenderTrustAccount().isAccountClosed()).isFalse();
    }

    @Test
    void isAccountClosed_Y_is_true() {
        final var offenderTrustAccount = new OffenderTrustAccount();
        offenderTrustAccount.setAccountClosedFlag("Y");
        assertThat(offenderTrustAccount.isAccountClosed()).isTrue();
    }

    @Test
    void isAccountClosed_N_is_false() {
        final var offenderTrustAccount = new OffenderTrustAccount();
        offenderTrustAccount.setAccountClosedFlag("N");
        assertThat(offenderTrustAccount.isAccountClosed()).isFalse();
    }
}
