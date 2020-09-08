package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class SentenceRepositoryTest {

    @Autowired
    private SentenceRepository repository;

    @BeforeEach
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void testGetMainOffenceDetailsSingleOffence() {
        final var offenceDetails = repository.getMainOffenceDetails(-1L);
        assertThat(offenceDetails).extracting(OffenceDetail::getOffenceDescription).containsExactly("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)");
    }

    @Test
    public final void testGetMainOffenceDetailsMultipleOffences() {
        final var offenceDetails = repository.getMainOffenceDetails(-7L);
        assertThat(offenceDetails).extracting(OffenceDetail::getOffenceDescription).containsExactly(
                "Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury",
                "Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.");
    }

    @Test
    public final void testGetMainOffenceDetailsInvalidBookingId() {
        final var offenceDetails = repository.getMainOffenceDetails(1001L);
        assertThat(offenceDetails).isEmpty();
    }

    @Test
    public final void testGetMainOffenceDetailsMultipleBookings() {
        final var offences = repository.getMainOffenceDetails(List.of(-1L, -7L));

        assertThat(offences).containsExactlyInAnyOrder(
                new OffenceDetail(-1L, "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)", "RV98011", "RV98"),
                new OffenceDetail(-7L, "Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.", "RC86360", "RC86"),
                new OffenceDetail(-7L, "Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury", "RC86355", "RC86")
        );
    }

    @Test
    public final void testGetOffenceHistory() {
        final var offenceDetails = repository.getOffenceHistory("A1234AA", true);
        assertThat(offenceDetails).extracting("bookingId", "offenceDate", "offenceRangeDate", "offenceDescription", "mostSerious", "primaryResultCode", "secondaryResultCode", "courtDate").containsExactly(
                Tuple.tuple(-1L, LocalDate.of(2017, 12, 24), null,
                        "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)",
                        true, "1004", null, LocalDate.of(2017, 7, 2)),
                Tuple.tuple(-1L, LocalDate.of(2018, 9, 1), LocalDate.of(2018, 9, 15),
                        "Cause another to use a vehicle where the seat belt buckle/other fastening was not maintained so that the belt could be readily fastened or unfastened/kept free from temporary or permanent obstruction/readily accessible to a person sitting in the seat.",
                        false, null, "1006", null)
        );
    }

    @Test
    public final void testGetOffenceHistoryOffenderWithoutConvictions() {
        final var offenceDetails = repository.getOffenceHistory("A1234AB", true);
        assertThat(offenceDetails).isEmpty();
    }

    @Test
    public final void testGetOffenceHistoryGetAllOffencesOffenderWithoutConvictions() {
        final var offenceDetails = repository.getOffenceHistory("A1234AB", false);
        assertThat(offenceDetails).extracting("bookingId", "primaryResultConviction", "primaryResultDescription",
                "secondaryResultConviction", "secondaryResultDescription","offenceDescription", "courtDate").containsExactly(
                Tuple.tuple(-2L, false, // no conviction result 1
                        "Adjourned for Consideration of an ASBO", // description of result 1
                        false, // no conviction result 2
                        null, // description of result 2 (no result 2 provided)
                        "Actual bodily harm", // offence description
                        LocalDate.of(2017, 2, 22))
        );
    }
}
