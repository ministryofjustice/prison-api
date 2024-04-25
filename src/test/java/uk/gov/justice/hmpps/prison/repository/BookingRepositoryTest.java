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
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository repository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertVisitDetails(final VisitDetails visitDetails) {
        assertThat(visitDetails).isNotNull();

        assertThat(visitDetails.getStartTime().toString()).isEqualTo("2016-12-11T14:30");
        assertThat(visitDetails.getEndTime().toString()).isEqualTo("2016-12-11T15:30");
        assertThat(visitDetails.getLeadVisitor()).isEqualTo("JESSY SMITH1");
        assertThat(visitDetails.getRelationship()).isEqualTo("FRI");
        assertThat(visitDetails.getRelationshipDescription()).isEqualTo("Friend");
        assertThat(visitDetails.getLocation()).isEqualTo("Visiting Room");
        assertThat(visitDetails.getEventStatus()).isEqualTo("CANC");
        assertThat(visitDetails.getEventStatusDescription()).isEqualTo("Cancelled");
        assertThat(visitDetails.getVisitType()).isEqualTo("SCON");
        assertThat(visitDetails.getVisitTypeDescription()).isEqualTo("Social Contact");
    }

    @Test
    public void testGetBookingVisitNextSameDay() {
        final var visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2016-12-11T14:00")).orElseThrow();

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitNextDifferentDay() {
        final var visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2016-12-10T17:00")).orElseThrow();

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitNextMultipleCandidates() {
        final var visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2017-11-12T00:00")).orElseThrow();

        assertThat(visit).isNotNull();
        assertThat(visit.getStartTime().toString()).isEqualTo("2017-11-13T14:30");
        assertThat(visit.getEndTime().toString()).isEqualTo("2017-11-13T15:30");
        assertThat(visit.getLeadVisitor()).isNull();
        assertThat(visit.getRelationship()).isNull();
    }

    @Test
    public void testGetBookingVisitNextNonexistentBooking() {
        final var visit = repository.getBookingVisitNext(-99L, LocalDateTime.parse("2016-12-11T16:00:00"));

        assertThat(visit).isEmpty();
    }

    @Test
    public void testGetBookingVisitNextLateDate() {
        final var visit = repository.getBookingVisitNext(-1L, LocalDateTime.parse("2021-12-11T16:00:00"));

        assertThat(visit).isEmpty();
    }

    @Test
    public void findBalancesForVisitOrdersAndPrivilegeVisitOrders() {
        final var visitBalances = repository.getBookingVisitBalances(-1L);

        assertThat(visitBalances.get()).isEqualTo(
                VisitBalances.builder().remainingVo(25).remainingPvo(2).latestIepAdjustDate(LocalDate.parse("2021-09-22")).latestPrivIepAdjustDate(LocalDate.parse("2021-10-22")).build());
    }

    @Test
    public void testGetBookingActivities() {
        final var results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null);

        assertThat(results).asList().hasSize(8);
        assertThat(results).asList().extracting("eventId", "payRate").contains(new Tuple(-11L, new BigDecimal("1.000")));
        assertThat(results).asList().extracting("locationCode").contains("CARP");
    }

    @Test
    public void testThatEventLocationIdIsPresent() {
        final var results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null);

        assertThat(results).asList().hasSize(8);
        assertThat(results).asList().extracting("eventId", "eventLocation", "eventLocationId")
                .contains(new Tuple(-11L, "Carpentry Workshop", -26L));
    }

    @Test
    public void testGetLatestBookingByBookingIdInvalidBookingId() {
        final var response = repository.getLatestBookingByBookingId(99999L);

        assertThat(response.isPresent()).isFalse();
    }

    @Test
    public void testGetLatestBookingByBookingIdHavingActiveBooking() {
        final Long bookingIdForActiveBooking = -5L;

        final var response = repository.getLatestBookingByBookingId(bookingIdForActiveBooking);

        assertThat(response.isPresent()).isTrue();

        final var summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo("A1234AE");
        assertThat(summary.getFirstName()).isEqualTo("DONALD");
        assertThat(summary.getMiddleNames()).isEqualTo("JEFFREY ROBERT");
        assertThat(summary.getLastName()).isEqualTo("MATTHEWS");
        assertThat(summary.getBookingId()).isEqualTo(bookingIdForActiveBooking);
        assertThat(summary.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("Y");
    }

    @Test
    public void testGetLatestBookingByBookingIdHavingInactiveBooking() {
        final Long bookingIdForInactiveBooking = -20L;

        final var response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        final var summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo("Z0020ZZ");
        assertThat(summary.getFirstName()).isEqualTo("BURT");
        assertThat(summary.getMiddleNames()).isNull();
        assertThat(summary.getLastName()).isEqualTo("REYNOLDS");
        assertThat(summary.getBookingId()).isEqualTo(bookingIdForInactiveBooking);
        assertThat(summary.getAgencyLocationId()).isEqualTo("OUT");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("N");
    }

    @Test
    public void testGetLatestBookingByBookingIdHavingLaterActiveBooking() {
        final Long bookingIdForInactiveBooking = -15L;

        final var response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        final var summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo("A1234AI");
        assertThat(summary.getFirstName()).isEqualTo("CHESTER");
        assertThat(summary.getMiddleNames()).isEqualTo("JAMES");
        assertThat(summary.getLastName()).isEqualTo("THOMPSON");
        assertThat(summary.getBookingId()).isNotEqualTo(bookingIdForInactiveBooking);
        assertThat(summary.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("Y");
    }

    @Test
    public void testGetLatestBookingByOffenderNoInvalidOffenderNo() {
        final var response = repository.getLatestBookingByOffenderNo("X9999XX");

        assertThat(response.isPresent()).isFalse();
    }

    @Test
    public void testGetLatestBookingByOffenderNoHavingActiveBooking() {
        final var offenderNoWithActiveBooking = "A1234AA";

        final var response = repository.getLatestBookingByOffenderNo(offenderNoWithActiveBooking);

        assertThat(response.isPresent()).isTrue();

        final var summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo(offenderNoWithActiveBooking);
        assertThat(summary.getFirstName()).isEqualTo("ARTHUR");
        assertThat(summary.getMiddleNames()).isEqualTo("BORIS");
        assertThat(summary.getLastName()).isEqualTo("ANDERSON");
        assertThat(summary.getBookingId()).isEqualTo(-1L);
        assertThat(summary.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("Y");
    }

    @Test
    public void testGetLatestBookingByOffenderNoHavingInactiveBooking() {
        final var offenderNoWithInactiveBooking = "Z0023ZZ";

        final var response = repository.getLatestBookingByOffenderNo(offenderNoWithInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        final var summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo(offenderNoWithInactiveBooking);
        assertThat(summary.getFirstName()).isEqualTo("RICHARD");
        assertThat(summary.getMiddleNames()).isNull();
        assertThat(summary.getLastName()).isEqualTo("GRAYSON");
        assertThat(summary.getBookingId()).isEqualTo(-23L);
        assertThat(summary.getAgencyLocationId()).isEqualTo("OUT");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("N");
    }

    @Test
    public void testUpdateAttendance() {
        final var updateAttendance = UpdateAttendance.builder()
                .eventOutcome("Great")
                .performance("Poor")
                .outcomeComment("Hi there")
                .build();

        repository.updateAttendance(-3L, -1L, updateAttendance, true, true);

        final var prisonerSchedules = scheduleRepository.getActivitiesAtLocation(-26L, null, null, null, null, false);
        final var first = prisonerSchedules.stream()
                .filter(ps -> ps.getEventId() != null && ps.getEventId() == -1L)
                .peek(ps -> {
                    assertThat(ps.getEventOutcome()).isEqualTo("Great");
                    assertThat(ps.getPerformance()).isEqualTo("Poor");
                    assertThat(ps.getOutcomeComment()).isEqualTo("Hi there");
                    assertThat(ps.getPaid()).isTrue();
                }).findFirst();
        assertThat(first.isPresent()).isTrue();
    }

    @Test
    public void testUpdateAttendanceInvalidActivityId() {
        final var ua = UpdateAttendance.builder()
                .eventOutcome("Great")
                .build();
        try {
            repository.updateAttendance(-3L, -111L, ua, false, false);
            fail("No exception thrown");
        } catch (final EntityNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Activity with booking Id -3 and activityId -111 not found");
        }
    }

    @Test
    public void testUpdateAttendanceInvalidBookingId() {
        final var ua = UpdateAttendance.builder()
                .eventOutcome("Great")
                .build();
        try {
            repository.updateAttendance(-333L, -1L, ua, false, false);
            fail("No exception thrown");
        } catch (final EntityNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Activity with booking Id -333 and activityId -1 not found");
        }
    }

    @Test
    public void testGetAttendanceEventDate() {
        assertThat(repository.getAttendanceEventDate(-1L)).isEqualTo("2017-09-11");
        assertThat(repository.getAttendanceEventDate(-2L)).isEqualTo("2017-09-12");
        assertThat(repository.getAttendanceEventDate(-3L)).isEqualTo("2017-09-13");
        assertThat(repository.getAttendanceEventDate(-4L)).isEqualTo("2017-09-14");
        assertThat(repository.getAttendanceEventDate(-5L)).isEqualTo("2017-09-15");
        assertThat(repository.getAttendanceEventDate(-101L)).isNull();
    }

    @Test
    public void testGetPayableAttendanceOutcomes() {
        assertThat(repository.getPayableAttendanceOutcome("PRISON_ACT", "NREQ"))
                .isEqualTo(PayableAttendanceOutcomeDto.builder()
                        .payableAttendanceOutcomeId(23L)
                        .eventType("PRISON_ACT")
                        .outcomeCode("NREQ")
                        .paid(true)
                        .authorisedAbsence(false)
                        .build()
                );
        assertThat(repository.getPayableAttendanceOutcome("PRISON_ACT", "COURT"))
                .isEqualTo(PayableAttendanceOutcomeDto.builder()
                        .payableAttendanceOutcomeId(77L)
                        .eventType("PRISON_ACT")
                        .outcomeCode("COURT")
                        .paid(false)
                        .authorisedAbsence(true)
                        .build()
                );
    }

    @Test
    public void testGetAlertCodesForBookingsFuture() {

        final var resultsFuture = repository.getAlertCodesForBookings(Arrays.asList(-1L, -2L, -16L),
                LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)));

        assertThat(resultsFuture.get(-1L)).asList().containsExactly("XA", "HC", "XTACT");
        assertThat(resultsFuture.get(-2L)).asList().containsExactly("HA", "XTACT");
        assertThat(resultsFuture.get(-16L)).isNull();
    }

    @Test
    public void testGetAlertCodesForBookingsPast() {

        final var resultsPast = repository.getAlertCodesForBookings(Arrays.asList(-1L, -2L, -16L),
                LocalDateTime.of(LocalDate.now().plusDays(-1), LocalTime.of(12, 0)));

        assertThat(resultsPast.get(-16L)).asList().containsExactly("OIOM");
    }

    @Test
    public void testGetAlertCodesForBookingsEmpty() {

        final var resultsPast = repository.getAlertCodesForBookings(Collections.emptyList(),
                LocalDateTime.now());

        assertThat(resultsPast).isEmpty();
    }

    @Test
    public void getOffenderSentenceCalculationsForPrisoner() {
        var sentenceCalculations = repository.getOffenderSentenceCalculationsForPrisoner("Z0024ZZ");
        assertThat(sentenceCalculations).isNotNull();
        assertThat(sentenceCalculations.size()).isEqualTo(1);
        assertThat(sentenceCalculations.getFirst().getOffenderNo()).isEqualTo("Z0024ZZ");
        assertThat(sentenceCalculations.getFirst().getAgencyLocationId()).isEqualTo("LEI");
        assertThat(sentenceCalculations.getFirst().getCalculationReason()).isEqualTo("New Sentence");
    }
}
