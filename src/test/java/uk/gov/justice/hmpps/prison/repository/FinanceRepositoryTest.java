package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
@WithMockAuthUser("ITAG_USER")
public class FinanceRepositoryTest {

    @Autowired
    private FinanceRepository repository;

    @Test
    public void testGetAccount() {
        final var account = repository.getBalances(-1L, "LEI");
        assertThat(account).isNotNull();
        assertThat(account.getCash()).isEqualByComparingTo("1.24");
        assertThat(account.getSpends()).isEqualByComparingTo("2.50");
        assertThat(account.getSavings()).isEqualByComparingTo("200.50");
    }

    @Test
    public void testGetAccountInvalidBookingId() {
        final var account = repository.getBalances(1001L, "LEI");
        assertThat(account).isNotNull();
        assertThat(account.getCash()).isNull();
        assertThat(account.getSpends()).isNull();
        assertThat(account.getSavings()).isNull();
    }

    @Test
    public void testWherePrisonerHasNoAccountInThatAgency() {
        final var account = repository.getBalances(-1L, "BXI");
        assertThat(account).isNotNull();
        assertThat(account.getCash()).isNull();
        assertThat(account.getSpends()).isNull();
        assertThat(account.getSavings()).isNull();
    }

    @Test
    public void testWherePrisonerHasSpendsInDifferentPrison() {
        final var account = repository.getBalances(-1L, "MDI");
        assertThat(account).isNotNull();
        assertThat(account.getSpends()).isEqualByComparingTo("12.75");
        assertThat(account.getCash()).isEqualByComparingTo("0.00");
        assertThat(account.getSavings()).isEqualByComparingTo("0.00");
    }
}
