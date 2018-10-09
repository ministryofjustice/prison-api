package net.syscon.elite.repository;

import net.syscon.elite.api.model.Award;
import net.syscon.elite.web.config.PersistenceConfigs;
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

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

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

        assertEquals("FORFEIT", awards.get(0).getSanctionCode());
        assertEquals("Forfeiture of Privileges", awards.get(0).getSanctionCodeDescription());
        assertNull(awards.get(0).getLimit());
        assertNull(awards.get(0).getMonths());
        assertEquals(30, awards.get(0).getDays().intValue());
        assertNull(awards.get(0).getComment());
        assertEquals("2016-11-08", awards.get(0).getEffectiveDate().toString());

        assertEquals("STOP_PCT", awards.get(1).getSanctionCode());
        assertEquals("Stoppage of Earnings (%)", awards.get(1).getSanctionCodeDescription());
        assertEquals(20.2, awards.get(1).getLimit().doubleValue(), 0.00001);
        assertEquals(4, awards.get(1).getMonths().intValue());
        assertEquals(5, awards.get(1).getDays().intValue());
        assertEquals("test comment", awards.get(1).getComment());
        assertEquals("2016-11-09", awards.get(1).getEffectiveDate().toString());
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

        assertEquals("ADA", awards.get(0).getSanctionCode());
        assertEquals("Additional Days Added", awards.get(0).getSanctionCodeDescription());
        assertNull(awards.get(0).getLimit());
        assertNull(awards.get(0).getMonths());
        assertNull(awards.get(0).getDays());
        assertNull(awards.get(0).getComment());
        assertEquals("SUSPENDED", awards.get(0).getStatus());
        assertEquals("Suspended", awards.get(0).getStatusDescription());
        assertEquals("2016-10-17", awards.get(0).getEffectiveDate().toString());

        assertEquals("CC", awards.get(1).getSanctionCode());
        assertEquals("Cellular Confinement", awards.get(1).getSanctionCodeDescription());
        assertNull(awards.get(1).getLimit());
        assertEquals(15, awards.get(1).getDays().intValue());
        assertNull(awards.get(1).getMonths());
        assertNull(awards.get(1).getComment());
        assertEquals("IMMEDIATE", awards.get(1).getStatus());
        assertEquals("Immediate", awards.get(1).getStatusDescription());
        assertEquals("2016-11-09", awards.get(1).getEffectiveDate().toString());
    }
}
/*
 (-1, 1,'ADA',    null,null,null,null,TO_DATE('2016-10-17', 'YYYY-MM-DD'),-10,'SUSPENDED',1);
I(-1, 2,'CC',     null,null,15,  null,TO_DATE('2016-11-09', 'YYYY-MM-DD'),-10,'IMMEDIATE',1);
 */