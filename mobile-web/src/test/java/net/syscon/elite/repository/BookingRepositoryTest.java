package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private static void assertVisitDetails(Visit visit) {
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
        NewAppointment appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-23T10:15:30"))
                .build();

        Long eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        ScheduledEvent event = repository.getBookingAppointment(-2L, eventId);

        assertThat(event).isNotNull();
        assertThat(event.getEventSubType()).isEqualTo(appt.getAppointmentType());
        assertThat(event.getEventLocation()).isEqualTo("Medical Centre");
        assertThat(event.getStartTime()).isEqualTo(appt.getStartTime());
        assertThat(event.getEventDate()).isEqualTo(appt.getStartTime().toLocalDate());
    }
    
    @Test
    public void testCreateBookingAppointmentWithEndComment() {
        NewAppointment appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-24T10:15:30"))
                .endTime(LocalDateTime.parse("2017-12-24T10:30:00"))
                .comment("Hi there")
                .build();

        Long eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        ScheduledEvent event = repository.getBookingAppointment(-2L, eventId);

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
        Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-11T16:00"));

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastDifferentDay() {
        Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2016-12-20T00:00"));

        assertVisitDetails(visit);
    }

    @Test
    public void testGetBookingVisitLastMultipleCandidates() {
        Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2017-12-07T00:00"));

        assertThat(visit).isNotNull();
        assertThat(visit.getStartTime().toString()).isEqualTo("2017-11-13T14:30");
        assertThat(visit.getEndTime().toString()).isEqualTo("2017-11-13T15:30");
        assertThat(visit.getLeadVisitor()).isNull();
        assertThat(visit.getRelationship()).isNull();
    }

    @Test
    public void testGetBookingVisitLastNonexistentBooking() {
        Visit visit = repository.getBookingVisitLast(-99L, LocalDateTime.parse("2016-12-11T16:00:00"));

        assertThat(visit).isNull();
    }

    @Test
    public void testGetBookingVisitLastEarlyDate() {
        Visit visit = repository.getBookingVisitLast(-1L, LocalDateTime.parse("2011-12-11T16:00:00"));

        assertThat(visit).isNull();
    }

    @Test
    public void testGetBookingActivities() {
        List<ScheduledEvent> results = repository.getBookingActivities(-2L, LocalDate.parse("2011-12-11"), LocalDate.now(), null, null);

        assertThat(results).asList().hasSize(8);
        assertThat(results).asList().extracting("eventId", "payRate").contains(new Tuple(-11L, new BigDecimal("1.000")));
    }

    @Test
    public void testGetLatestBookingByBookingIdInvalidBookingId() {
        Optional<OffenderSummary> response = repository.getLatestBookingByBookingId(99999L);

        assertThat(response.isPresent()).isFalse();
    }

    @Test
    public void testGetLatestBookingByBookingIdHavingActiveBooking() {
        Long bookingIdForActiveBooking = -5L;

        Optional<OffenderSummary> response = repository.getLatestBookingByBookingId(bookingIdForActiveBooking);

        assertThat(response.isPresent()).isTrue();

        OffenderSummary summary = response.get();

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
        Long bookingIdForInactiveBooking = -20L;

        Optional<OffenderSummary> response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        OffenderSummary summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo("Z0020ZZ");
        assertThat(summary.getFirstName()).isEqualTo("BURT");
        assertThat(summary.getMiddleNames()).isNull();
        assertThat(summary.getLastName()).isEqualTo("REYNOLDS");
        assertThat(summary.getBookingId()).isEqualTo(bookingIdForInactiveBooking);
        assertThat(summary.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("N");
    }

    @Test
    public void testGetLatestBookingByBookingIdHavingLaterActiveBooking() {
        Long bookingIdForInactiveBooking = -15L;

        Optional<OffenderSummary> response = repository.getLatestBookingByBookingId(bookingIdForInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        OffenderSummary summary = response.get();

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
        Optional<OffenderSummary> response = repository.getLatestBookingByOffenderNo("X9999XX");

        assertThat(response.isPresent()).isFalse();
    }

    @Test
    public void testGetLatestBookingByOffenderNoHavingActiveBooking() {
        String offenderNoWithActiveBooking = "A1234AA";

        Optional<OffenderSummary> response = repository.getLatestBookingByOffenderNo(offenderNoWithActiveBooking);

        assertThat(response.isPresent()).isTrue();

        OffenderSummary summary = response.get();

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
        String offenderNoWithInactiveBooking = "Z0023ZZ";

        Optional<OffenderSummary> response = repository.getLatestBookingByOffenderNo(offenderNoWithInactiveBooking);

        assertThat(response.isPresent()).isTrue();

        OffenderSummary summary = response.get();

        assertThat(summary.getOffenderNo()).isEqualTo(offenderNoWithInactiveBooking);
        assertThat(summary.getFirstName()).isEqualTo("RICHARD");
        assertThat(summary.getMiddleNames()).isNull();
        assertThat(summary.getLastName()).isEqualTo("GRAYSON");
        assertThat(summary.getBookingId()).isEqualTo(-23L);
        assertThat(summary.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(summary.getCurrentlyInPrison()).isEqualTo("N");
    }

    @Test
    public void testUpdateAttendance() {
        UpdateAttendance updateAttendance = UpdateAttendance.builder()
                .eventOutcome("Great")
                .performance("Poor")
                .outcomeComment("Hi there")
                .build();

        repository.updateAttendance(-3L, -1L, updateAttendance, true, true);

        List<PrisonerSchedule> prisonerSchedules = scheduleRepository.getLocationActivities(-26L, null, null, null, null);
        final Optional<PrisonerSchedule> first = prisonerSchedules.stream()
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
        UpdateAttendance ua = UpdateAttendance.builder()
                .eventOutcome("Great")
                .build();
        try {
            repository.updateAttendance(-3L, -111L, ua, false, false);
            fail("No exception thrown");
        } catch (EntityNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Activity with booking Id -3 and activityId -111 not found");
        }
    }

    @Test
    public void testUpdateAttendanceInvalidBookingId() {
        UpdateAttendance ua = UpdateAttendance.builder()
                .eventOutcome("Great")
                .build();
        try {
            repository.updateAttendance(-333L, -1L, ua, false, false);
            fail("No exception thrown");
        } catch (EntityNotFoundException e) {
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
}
