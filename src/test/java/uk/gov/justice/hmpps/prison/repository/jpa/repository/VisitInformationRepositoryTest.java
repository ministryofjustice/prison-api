package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository.Prison;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({HmppsAuthenticationHolder.class, AuditorAwareImpl.class})
public class VisitInformationRepositoryTest {

    @Autowired
    private VisitInformationRepository repository;

    @Test
    public void findAll() {
        final var pageable = PageRequest.of(0, 20);
        final var visits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).build(), pageable);

        assertThat(visits).hasSize(15);
        assertThat(visits).extracting(VisitInformation::getVisitId).containsExactly(-3L, -2L, -4L, -5L, -1L, -6L, -8L, -7L, -10L, -9L, -13L, -14L, -12L, -11L, -15L);
        assertThat(visits).extracting(VisitInformation::getCancellationReason).containsExactly("NSHOW", null, null, null, null, null, null, null, "VISCANC", null, null, null, null, null, "NSHOW");
        assertThat(visits).extracting(VisitInformation::getCancelReasonDescription).containsExactly("Visitor Did Not Arrive", null, null, null, null, null, null, null, "Visitor Cancelled", null, null, null, null, null, "Visitor Did Not Arrive");
        assertThat(visits).extracting(VisitInformation::getEventStatus).containsExactly("CANC", "SCH", "SCH", "SCH", "SCH", "SCH", "EXP", "SCH", "CANC", "COMP", "SCH", "SCH", "SCH", "SCH", "CANC");
        assertThat(visits).extracting(VisitInformation::getEventStatusDescription).containsExactly("Cancelled", "Scheduled (Approved)", "Scheduled (Approved)", "Scheduled (Approved)", "Scheduled (Approved)", "Scheduled (Approved)", "Expired", "Scheduled (Approved)", "Cancelled", "Completed", "Scheduled (Approved)", "Scheduled (Approved)", "Scheduled (Approved)", "Scheduled (Approved)", "Cancelled");
        assertThat(visits).extracting(VisitInformation::getEventOutcome).containsExactly("ABS", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ATT", "ABS", "ATT", "ATT", "ATT", "ATT", "ATT", "ABS");
        assertThat(visits).extracting(VisitInformation::getEventOutcomeDescription).containsExactly("Absence", "Attended", "Attended", "Attended", "Attended", "Attended", "Attended", "Attended", "Absence", "Attended", "Attended", "Attended", "Attended", "Attended", "Absence");
        assertThat(visits).extracting(VisitInformation::getStartTime).containsExactly(LocalDateTime.parse("2017-12-10T14:30"), LocalDateTime.parse("2017-11-13T14:30"), LocalDateTime.parse("2017-10-13T14:30"), LocalDateTime.parse("2017-09-15T14:00"), LocalDateTime.parse("2017-09-12T14:30"), LocalDateTime.parse("2017-09-10T14:30"), LocalDateTime.parse("2017-08-10T14:30"), LocalDateTime.parse("2017-07-10T14:30"), LocalDateTime.parse("2017-06-10T14:30"), LocalDateTime.parse("2017-05-10T14:30"), LocalDateTime.parse("2017-04-10T14:30"), LocalDateTime.parse("2017-03-10T14:30"), LocalDateTime.parse("2017-02-10T14:30"), LocalDateTime.parse("2017-01-10T14:30"), LocalDateTime.parse("2016-12-11T14:30"));
        assertThat(visits).extracting(VisitInformation::getEndTime).containsExactly(LocalDateTime.parse("2017-12-10T15:30"), LocalDateTime.parse("2017-11-13T15:30"), LocalDateTime.parse("2017-10-13T15:30"), LocalDateTime.parse("2017-09-15T16:00"), LocalDateTime.parse("2017-09-12T15:30"), LocalDateTime.parse("2017-09-10T15:30"), LocalDateTime.parse("2017-08-10T15:30"), LocalDateTime.parse("2017-07-10T15:30"), LocalDateTime.parse("2017-06-10T15:30"), LocalDateTime.parse("2017-05-10T16:30"), LocalDateTime.parse("2017-04-10T15:30"), LocalDateTime.parse("2017-03-10T16:30"), LocalDateTime.parse("2017-02-10T15:30"), LocalDateTime.parse("2017-01-10T15:30"), LocalDateTime.parse("2016-12-11T15:30"));
        assertThat(visits).extracting(VisitInformation::getLocation).containsExactly("Visiting Room", "Visiting Room", "Visiting Room", "Chapel", "Visiting Room", "Visiting Room", "Visiting Room", "Visiting Room", "Visiting Room", "Chapel", "Visiting Room", "Chapel", "Visiting Room", "Visiting Room", "Visiting Room");
        assertThat(visits).extracting(VisitInformation::getVisitType).containsExactly("SCON", "SCON", "SCON", "OFFI", "SCON", "SCON", "SCON", "SCON", "SCON", "OFFI", "SCON", "OFFI", "SCON", "SCON", "SCON");
        assertThat(visits).extracting(VisitInformation::getVisitTypeDescription).containsExactly("Social Contact", "Social Contact", "Social Contact", "Official Visit", "Social Contact", "Social Contact", "Social Contact", "Social Contact", "Social Contact", "Official Visit", "Social Contact", "Official Visit", "Social Contact", "Social Contact", "Social Contact");
        assertThat(visits).extracting(VisitInformation::getLeadVisitor).containsExactly("JESSY SMITH1", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "JESSY SMITH1");
        assertThat(visits).extracting(VisitInformation::getVisitStatus).containsExactly("SCH", "SCH", "SCH", "SCH", "SCH", "SCH", "SCH", "SCH", "CANC", "VDE", "SCH", "SCH", "SCH", "SCH", "SCH");
        assertThat(visits).extracting(VisitInformation::getVisitStatusDescription).containsExactly("Scheduled", "Scheduled", "Scheduled", "Scheduled", "Scheduled", "Scheduled", "Scheduled", "Scheduled", "Cancelled", "Visitor Declined Entry", "Scheduled", "Scheduled", "Scheduled", "Scheduled", "Scheduled");
        assertThat(visits).extracting(VisitInformation::getSearchType).containsExactly(null, null, null, null, null, null, null, null, null, null, null, null, null, "RUB_A", null);
        assertThat(visits).extracting(VisitInformation::getSearchTypeDescription).containsExactly(null, null, null, null, null, null, null, null, null, null, null, null, null, "Rubdown Level A", null);
    }

    @Test
    public void findAll_filterByType() {
        final var pageable = PageRequest.of(0, 20);
        final var visits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitType("OFFI").build(), pageable);

        assertThat(visits).hasSize(3);
        assertThat(visits).extracting(VisitInformation::getVisitId).containsExactly(-5L, -9L, -14L);
        assertThat(visits).extracting(VisitInformation::getEventOutcomeDescription).containsExactly("Attended", "Attended", "Attended");
        assertThat(visits).extracting(VisitInformation::getVisitType).allMatch((it) -> it.equals("OFFI"));

    }

    @Test
    public void findAll_filterByDates() {
        final var pageable = PageRequest.of(0, 20);
        final var visits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).fromDate(LocalDate.of(2017, 9, 1)).toDate(LocalDate.of(2017, 10, 1)).build(), pageable);

        assertThat(visits).hasSize(3);
        assertThat(visits).extracting(VisitInformation::getVisitId).containsExactly(-5L, -1L, -6L);
        assertThat(visits).extracting(VisitInformation::getEventOutcome).containsExactly("ATT", "ATT", "ATT");
        assertThat(visits).extracting(VisitInformation::getEventOutcomeDescription).containsExactly("Attended", "Attended", "Attended");
        assertThat(visits).extracting(VisitInformation::getVisitType).containsExactly("OFFI", "SCON", "SCON");
        assertThat(visits).extracting(VisitInformation::getVisitTypeDescription).containsExactly("Official Visit", "Social Contact", "Social Contact");

    }

    @Test
    public void findAll_filterByStatus() {
        final var pageable = PageRequest.of(0, 20);

        final var scheduledVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitStatus("SCH").build(), pageable);
        assertThat(scheduledVisits).hasSize(13);
        assertThat(scheduledVisits).extracting(VisitInformation::getVisitStatus).allMatch((it) -> it.equals("SCH"));

        final var cancelledVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).visitStatus("CANC").build(), pageable);
        assertThat(cancelledVisits).hasSize(1);
        assertThat(cancelledVisits).extracting(VisitInformation::getVisitStatus).allMatch((it) -> it.equals("CANC"));
    }

    @Test
    public void findAll_filterByAgency() {
        final var pageable = PageRequest.of(0, 20);

        final var leicesterVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).prisonId("LEI").build(), pageable);
        assertThat(leicesterVisits).hasSize(13);
        assertThat(leicesterVisits).extracting(VisitInformation::getPrisonDescription).allMatch((it) -> it.equals("LEEDS"));

        final var moorlandVisits = repository.findAll(VisitInformationFilter.builder().bookingId(-1L).prisonId("MDI").build(), pageable);
        assertThat(moorlandVisits).hasSize(1);
        assertThat(moorlandVisits).extracting(VisitInformation::getPrisonDescription).allMatch((it) -> it.equals("MOORLAND"));
    }

    @Test
    public void findByBookingIdGroupByPrisonId() {
        final var prisons = repository.findByBookingIdGroupByPrisonId(-1);
        assertThat(prisons).extracting(Prison::getPrisonId).containsExactly("LEI", "MDI", "BXI");
        assertThat(prisons).extracting(Prison::getPrisonDescription).containsExactly("LEEDS", "MOORLAND", "BRIXTON");
    }

    @Nested
    public class countByBookingId {
        @Test
        public void countByBookingId() {
            final var visits = repository.countByBookingId(-1L);
            assertThat(visits).isEqualTo(15);
        }
        @Test
        public void countByBookingId_notfound() {
            final var visits = repository.countByBookingId(-12345L);
            assertThat(visits).isEqualTo(0);
        }
        @Test
        public void countByBookingId_other() {
            final var visits = repository.countByBookingId(-2L);
            assertThat(visits).isEqualTo(1);
        }
    }
}


