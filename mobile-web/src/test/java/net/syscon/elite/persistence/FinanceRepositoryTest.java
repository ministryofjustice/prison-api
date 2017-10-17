package net.syscon.elite.persistence;

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.web.config.PersistenceConfigs;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.*;

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
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void testGetAccount() {
        final Account account = repository.getBalances(-1L);
        assertNotNull(account);
        assertEquals("1.24", account.getCash().toString());
        assertEquals("2.50", account.getSpends().toString());
        assertEquals("200.50", account.getSavings().toString());
    }

    @Test
    public final void testGetAccountInvalidBookingId() {
        final Account account = repository.getBalances(1001L);
        assertNotNull(account);
        assertNull(account.getCash());
        assertNull(account.getSpends());
        assertNull(account.getSavings());
    }
}
