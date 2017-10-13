package net.syscon.elite.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.web.config.PersistenceConfigs;

@ActiveProfiles("nomis,nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class FinanceRepositoryTest {

    @Autowired
    private FinanceRepository repository;

    @Before
    public final void setup() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void testGetAccount() {
        final Optional<Account> account = repository.getAccount(-1L, Collections.singleton("LEI"));
        assertThat(account).isPresent();
        assertEquals(124L, account.get().getCash().longValue());
        assertEquals(250L, account.get().getSpends().longValue());
        assertEquals(20050L, account.get().getSavings().longValue());
    }

    @Test
    public final void testGetAccount_no_booking_id() {
        final Optional<Account> account = repository.getAccount(1001L, Collections.singleton("LEI"));
        assertThat(account).isEmpty();
    }

    @Test
    public final void testGetAccount_no_caseload() {
        final Optional<Account> account = repository.getAccount(1L, Collections.singleton("madeup"));
        assertThat(account).isEmpty();
    }
}
