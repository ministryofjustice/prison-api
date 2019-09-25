package net.syscon.elite.repository;

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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
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
        final var offenceDetails = repository.getMainOffenceDetails(-1L);
        assertNotNull(offenceDetails);
        assertEquals(1, offenceDetails.size());
        assertEquals("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)", offenceDetails.get(0).getOffenceDescription());
    }

    @Test
    public final void testGetMainOffenceDetailsMultipleOffences() {
        final var offenceDetails = repository.getMainOffenceDetails(-7L);
        assertNotNull(offenceDetails);
        assertEquals(2, offenceDetails.size());
        assertEquals("Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury", offenceDetails.get(0).getOffenceDescription());
        assertEquals("Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.", offenceDetails.get(1).getOffenceDescription());
    }

    @Test
    public final void testGetMainOffenceDetailsInvalidBookingId() {
        final var offenceDetails = repository.getMainOffenceDetails(1001L);
        assertNotNull(offenceDetails);
        assertTrue(offenceDetails.isEmpty());
    }

    @Test
    public final void testGetOffenceHistory() {
        final var offenceDetails = repository.getOffenceHistory("A1234AA");

        assertThat(offenceDetails).asList().extracting("bookingId", "offenceDate", "offenceRangeDate", "offenceDescription", "mostSerious").containsExactly(
                Tuple.tuple(-1L, LocalDate.of(2017, 12, 24), null,
                        "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)",
                        true),
                Tuple.tuple(-1L, LocalDate.of(2018, 9, 1), LocalDate.of(2018, 9, 15),
                        "Cause another to use a vehicle where the seat belt buckle/other fastening was not maintained so that the belt could be readily fastened or unfastened/kept free from temporary or permanent obstruction/readily accessible to a person sitting in the seat.",
                        false)
        );
    }

    @Test
    public final void testGetOffenceHistoryNoConviction() {
        final var offenceDetails = repository.getOffenceHistory("A1234AB");

        assertThat(offenceDetails).asList().isEmpty();
    }
}
