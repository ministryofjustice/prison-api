package net.syscon.elite.repository;

import net.syscon.elite.api.model.Award;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.assertj.core.groups.Tuple;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AdjudicationsRepositoryTest {

    @Autowired
    private AdjudicationsRepository repository;

    @Before
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetDetailsMultiple() {
        List<Award> awards = repository.findAwards(-3L);
        assertNotNull(awards);
        assertEquals(2, awards.size());

        assertThat(awards).asList()
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(Tuple.tuple("FORFEIT", "Forfeiture of Privileges", null, null, 30, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 8)),
                        Tuple.tuple("STOP_PCT", "Stoppage of Earnings (%)", BigDecimal.valueOf(2020L, 2), 4, 5, "test comment", "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }

    @Test
    public void testGetDetailsInvalidBookingId() {
        List<Award> awards = repository.findAwards(1001L);
        assertNotNull(awards);
        assertTrue(awards.isEmpty());
    }

    @Test
    public void testGetDetailsMultiple2() {
        List<Award> awards = repository.findAwards(-1L);
        assertNotNull(awards);
        assertEquals(2, awards.size());

        assertThat(awards).asList()
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(Tuple.tuple("ADA", "Additional Days Added", null, null, null, null, "SUSPENDED", "Suspended", LocalDate.of(2016, 10, 17)),
                        Tuple.tuple("CC", "Cellular Confinement", null, null, 15, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }
}
