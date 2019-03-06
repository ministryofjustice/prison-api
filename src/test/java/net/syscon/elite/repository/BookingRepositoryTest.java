package net.syscon.elite.repository;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.OffenderSentenceTerms;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.api.model.Visit;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.PayableAttendanceOutcomeDto;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository repository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Before
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertVisitDetails(final Visit visit) {
        assertThat(visit).isNotNull();

        assertThat(visit.getStartTime().toString()).isEqualTo("2016-12-11T14:30");
        assertThat(visit.getEndTime().toString()).isEqualTo("2016-12-11T15:30");
        assertThat(visit.getEventOutcome()).isEqualTo("ABS");
        assertThat(visit.getEventOutcomeDescription()).isEqualTo("Absence");
        assertThat(visit.getLeadVisitor()).isEqualTo("JESSY SMITH1");
        assertThat(visit.getRelationship()).isEqualTo("UN");
        assertThat(visit.getRelationshipDescription()).isEqualTo("Uncle");
        assertThat(visit.getLocation()).isEqualTo("Visiting Room");
        assertThat(visit.getEventStatus()).isEqualTo("CANC");
        assertThat(visit.getEventStatusDescription()).isEqualTo("Cancelled");
        assertThat(visit.getCancellationReason()).isEqualTo("NSHOW");
        assertThat(visit.getCancelReasonDescription()).isEqualTo("Visitor Did Not Arrive");
        assertThat(visit.getVisitType()).isEqualTo("SCON");
        assertThat(visit.getVisitTypeDescription()).isEqualTo("Social Contact");
    }

    @Test
    public void testCreateBookingAppointment() {
        final var appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-23T10:15:30"))
                .build();

        final var eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        final var event = repository.getBookingAppointment(-2L, eventId);

        assertThat(event).isNotNull();
        assertThat(event.getEventSubType()).isEqualTo(appt.getAppointmentType());
        assertThat(event.getEventLocation()).isEqualTo("Medical Centre");
        assertThat(event.getStartTime()).isEqualTo(appt.getStartTime());
        assertThat(event.getEventDate()).isEqualTo(appt.getStartTime().toLocalDate());
    }

    @Test
    public void testCreateBookingAppointmentWithEndComment() {
        final var appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-24T10:15:30"))
                .endTime(LocalDateTime.parse("2017-12-24T10:30:00"))
                .comment("Hi there")
                .build();

        final var eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        final var event = repository.getBookingAppointment(-2L, eventId);

        assertThat(event).isNotNull();
        assertThat(event.getEventSubType()).isEqualTo(appt.getAppointmentType());
        assertThat(event.getEventLocation()).isEqualTo("Medical Centre");
        assertThat(event.getStartTime()).isEqualTo(appt.getStartTime());
        assertThat(event.getEndTime()).isEqualTo(appt.getEndTime());
        assertThat(event.getEventSourceDesc()).isEqualTo(appt.getComment());
        assertThat(event.getEventDate()).isEqualTo(appt.getStartTime().toLocalDate());
    }

    @Test
    public void testGetBookingVisitLastSameDay() {
        final var visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-11T16:00"));

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastDifferentDay() {
        final var visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-20T00:00"));

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastMultipleCandidates() {
        final var visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2017-12-07T00:00"));

        assertThat(visit).isNotNull();
        assertThat(visit.getStartTime().toString()).isEqualTo("2017-11-13T14:30");
        assertThat(visit.getEndTime().toString()).isEqualTo("2017-11-13T15:30");
        assertThat(visit.getLeadVisitor()).isNull();
        assertThat(visit.getRelationship()).isNull();
    }

    @Test
    public void testGetBookingVisitLastNonexistentBooking() {
        final var visit = repository.getBookingVisitLast(-99L, LocalDateTime.parse("2016-12-11T16:00:00"));

        assertThat(visit).isNull();
    }

    @Test
    public void testGetBookingVisitLastEarlyDate() {
        final var visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2011-12-11T16:00:00"));

        assertThat(visit).isNull();
    }

    @Test
    public void testGetBookingActivities() {
        final var results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null);

        assertThat(results).asList().hasSize(8);
        assertThat(results).asList().extracting("eventId", "payRate").contains(new Tuple(-11L, new BigDecimal("1.000")));
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
        assertThat(summary.getLastName()).isEqualTo("DUCK");
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

        final var prisonerSchedules = scheduleRepository.getLocationActivities(-26L, null, null, null, null);
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

        assertThat(resultsFuture.get(-1L)).asList().containsExactly("XA", "HC");
        assertThat(resultsFuture.get(-2L)).asList().containsExactly("HA");
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
    public void testGetOffenderSentenceTerms() {

        final var results = repository.getOffenderSentenceTerms(-2L, "IMP");

        assertThat(results)
                .asList()
                .containsExactlyInAnyOrder(
                        new OffenderSentenceTerms(-2L, LocalDate.of(2016, 11, 22), null, 6, null, null, false),
                        new OffenderSentenceTerms(-2L, LocalDate.of(2017, 5, 22), 2, null, null, null, false),
                        new OffenderSentenceTerms(-2L, LocalDate.of(2017, 6, 22), null, null, 2, 3, false),
                        new OffenderSentenceTerms(-2L, LocalDate.of(2017, 7, 22), 25, null, null, null, true)
                );
    }

    @Test
    public void findNoBookingIdsInAgency() {
        assertThat(repository.findBookingsIdsInAgency(Collections.emptyList(), "LEI")).isEmpty();
    }

    @Test
    public void findBookingIdsInLEI() {
        assertThat(repository.findBookingsIdsInAgency(Arrays.asList(-1L, -2L, -13L, -14L), "LEI")).containsExactlyInAnyOrder(-1L, -2L);
    }

    @Test
    public void findBookingIdsInOUT() {
        assertThat(repository.findBookingsIdsInAgency(Arrays.asList(-1L, -2L, -13L, -14L), "OUT")).containsExactlyInAnyOrder(-13L, -14L);
    }

    @Test
    public void createMultipleAppointments() {
        final var now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var in1Hour = now.plusHours(1L);
        final var today = now.toLocalDate();

        final var bookingIds = Arrays.asList(-31L, -32L);

        // Given
        final var scheduledEventsBefore = repository.getBookingAppointments(bookingIds, today, today, null, Order.ASC);
        assertThat(scheduledEventsBefore).hasSize(0);

        // When
        final var defaults = AppointmentDefaults
                .builder()
                .locationId(-25L) // LEI-CHAP
                .appointmentType("ACTI") // Activity
                .build();

        final var appointments = bookingIds
                .stream()
                .map(id -> AppointmentDetails
                        .builder()
                        .bookingId(id)
                        .startTime(now)
                        .endTime(in1Hour)
                        .comment("Comment")
                        .build())
                .collect(Collectors.toList());

        repository.createMultipleAppointments(appointments, defaults, "LEI");

        // Then
        final var scheduledEventsAfter = repository.getBookingAppointments(bookingIds, today, today, null, Order.ASC);

        assertThat(scheduledEventsAfter)
                .extracting("bookingId", "eventType", "eventSubType", "eventDate", "startTime", "endTime", "eventLocation")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(-31L, "APP", "ACTI", today, now, in1Hour, "Chapel"),
                        Tuple.tuple(-32L, "APP", "ACTI", today, now, in1Hour, "Chapel"));
    }
}
