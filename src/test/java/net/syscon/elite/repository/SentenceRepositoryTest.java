package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenceDetail;
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
public class SentenceRepositoryTest {

    @Autowired
    private SentenceRepository repository;

    @Before
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void testGetMainOffenceDetailsSingleOffence() {
        List<OffenceDetail> offenceDetails = repository.getMainOffenceDetails(-1L);
        assertNotNull(offenceDetails);
        assertEquals(1, offenceDetails.size());
        assertEquals("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)", offenceDetails.get(0).getOffenceDescription());
    }

    @Test
    public final void testGetMainOffenceDetailsMultipleOffences() {
        List<OffenceDetail> offenceDetails = repository.getMainOffenceDetails(-7L);
        assertNotNull(offenceDetails);
        assertEquals(2, offenceDetails.size());
        assertEquals("Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury", offenceDetails.get(0).getOffenceDescription());
        assertEquals("Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.", offenceDetails.get(1).getOffenceDescription());
    }

    @Test
    public final void testGetMainOffenceDetailsInvalidBookingId() {
        List<OffenceDetail> offenceDetails = repository.getMainOffenceDetails(1001L);
        assertNotNull(offenceDetails);
        assertTrue(offenceDetails.isEmpty());
    }
}
