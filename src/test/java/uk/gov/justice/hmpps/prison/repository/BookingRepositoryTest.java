package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @Autowired
    NamedParameterJdbcTemplate template;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    private static void assertVisitDetails(final VisitDetails visitDetails) {
        assertThat(visitDetails).isNotNull();

        assertThat(visitDetails.getStartTime().toString()).isEqualTo("2016-12-11T14:30");
        assertThat(visitDetails.getEndTime().toString()).isEqualTo("2016-12-11T15:30");
        assertThat(visitDetails.getEventOutcome()).isEqualTo("ABS");
        assertThat(visitDetails.getEventOutcomeDescription()).isEqualTo("Absence");
        assertThat(visitDetails.getLeadVisitor()).isEqualTo("JESSY SMITH1");
        assertThat(visitDetails.getRelationship()).isEqualTo("FRI");
        assertThat(visitDetails.getRelationshipDescription()).isEqualTo("Friend");
        assertThat(visitDetails.getLocation()).isEqualTo("Visiting Room");
        assertThat(visitDetails.getEventStatus()).isEqualTo("CANC");
        assertThat(visitDetails.getEventStatusDescription()).isEqualTo("Cancelled");
        assertThat(visitDetails.getCancellationReason()).isEqualTo("NSHOW");
        assertThat(visitDetails.getCancelReasonDescription()).isEqualTo("Visitor Did Not Arrive");
        assertThat(visitDetails.getVisitType()).isEqualTo("SCON");
        assertThat(visitDetails.getVisitTypeDescription()).isEqualTo("Social Contact");
    }

    @Test
    public void testCreateBookingAppointment() {
        final var appt = NewAppointment.builder()
                .appointmentType("APT_TYPE")
                .locationId(-29L)
                .startTime(LocalDateTime.parse("2017-12-23T10:15:30"))
                .build();

        final var eventId = repository.createBookingAppointment(-2L, appt, "LEI");

        final var event = repository.getBookingAppointmentByEventId(eventId).get();

        assertThat(event).isNotNull();
        assertThat(event.getEventSubType()).isEqualTo(appt.getAppointmentType());
        assertThat(event.getEventLocation()).isEqualTo("Medical Centre");
        assertThat(event.getEventLocationId()).isEqualTo(-29L);
        assertThat(event.getAgencyId()).isEqualTo("LEI");
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

        final var event = repository.getBookingAppointmentByEventId(eventId).get();

        assertThat(event).isNotNull();
        assertThat(event.getEventSubType()).isEqualTo(appt.getAppointmentType());
        assertThat(event.getEventLocation()).isEqualTo("Medical Centre");
        assertThat(event.getEventLocationId()).isEqualTo(-29L);
        assertThat(event.getAgencyId()).isEqualTo("LEI");
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
    public void findBalancesForVisitOrdersAndPrivilageVisitOrders() {
        final var visitBalances = repository.getBookingVisitBalances(-1L);

        assertThat(visitBalances).get().isEqualToIgnoringGivenFields(
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
    public void createAppointment() {
        final var now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var in1Hour = now.plusHours(1L);
        final var today = now.toLocalDate();

        final var bookingId = -31L;

        // Given
        final var scheduledEventsBefore = repository.getBookingAppointments(bookingId, today, today, null, Order.ASC);
        assertThat(scheduledEventsBefore).hasSize(0);

        // When
        final var defaults = AppointmentDefaults
                .builder()
                .locationId(-25L) // LEI-CHAP
                .appointmentType("ACTI") // Activity
                .build();

        final var appointment = AppointmentDetails
                .builder()
                .bookingId(bookingId)
                .startTime(now)
                .endTime(in1Hour)
                .comment("Comment")
                .build();

        final var assignedId = repository.createAppointment(appointment, defaults, "LEI");

        // Then
        final var scheduledEventsAfter = repository.getBookingAppointments(bookingId, today, today, null, Order.ASC);
        final var bookingAppointmentAfter = repository.getBookingAppointmentByEventId(assignedId);

        assertThat(scheduledEventsAfter)
                .extracting("bookingId", "eventType", "eventSubType", "eventDate", "startTime", "endTime", "eventLocation")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(-31L, "APP", "ACTI", today, now, in1Hour, "Chapel"));
        assertThat(bookingAppointmentAfter).isPresent();
        assertThat(bookingAppointmentAfter.get().getBookingId()).isEqualTo(-31L);
    }

    @Test
    public void getBookingAppointmentByEventId_noAppointment() {
        assertThat(repository.getBookingAppointmentByEventId(-999)).isEmpty();
    }

    @Test
    public void getBookingAppointmentByEventId() {
        final var startTime = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS);  // Drop nanos.
        final var endTime = startTime.plusMinutes(30);
        final var bookingId = -30L;
        final var locationId = -28L;// LEI_VIS. This should really be a location with location usage 'VIDE' but I don't think it matters for this test.

        final var newAppointment = NewAppointment.builder()
                .appointmentType("VLB")
                .startTime(startTime)
                .endTime(endTime)
                .locationId(locationId)
                .build();

        final var id = repository.createBookingAppointment(bookingId, newAppointment, "LEI");

        // Could commit and start a new transaction here, but I don't think it is necessary.

        assertThat(repository.getBookingAppointmentByEventId(id))
                .hasValueSatisfying(se -> assertThat(se)
                        .extracting("bookingId", "eventId", "startTime", "endTime", "eventLocationId", "createUserId")
                        .containsExactly(bookingId, id, startTime, endTime, locationId, "SA"));
    }

    @Test
    public void deleteBookingAppointment() {
        // Do this test in a single transaction. Good enough for JDBC.
        final var startTime = LocalDateTime.now().plusDays(2);
        final var endTime = startTime.plusMinutes(30);
        final var bookingId = -30L;
        final var locationId = -28L;// LEI-LEI_VIS. This should really be a location with location usage 'VIDE' but I don't think it matters for this test.

        final var newAppointment = NewAppointment.builder()
                .appointmentType("VLB")
                .startTime(startTime)
                .endTime(endTime)
                .locationId(locationId)
                .build();

        final var id = repository.createBookingAppointment(bookingId, newAppointment, "LEI");

        assertThat(repository.getBookingAppointmentByEventId(id)).isNotEmpty();

        repository.deleteBookingAppointment(id);

        assertThat(repository.getBookingAppointmentByEventId(id)).isEmpty();
    }

    @Test
    public void updateBookingAppointmentComment() {
       final var startTime = LocalDateTime.now().plusDays(2);
        final var endTime = startTime.plusMinutes(30);
        final var bookingId = -30L;
        final var locationId = -28L;// LEI-LEI_VIS. This should really be a location with location usage 'VIDE' but I don't think it matters for this test.

        final var newAppointment = NewAppointment.builder()
            .appointmentType("VLB")
            .startTime(startTime)
            .endTime(endTime)
            .locationId(locationId)
            .build();

        final var id = repository.createBookingAppointment(bookingId, newAppointment, "LEI");

        assertThat(repository.getBookingAppointmentByEventId(id)).isNotEmpty();

        assertThat(repository.updateBookingAppointmentComment(id, "Test comment")).isTrue();

        assertThat(repository.getBookingAppointmentByEventId(id))
            .get()
            .extracting("eventSourceDesc")
            .isEqualTo("Test comment");

        assertThat(repository.updateBookingAppointmentComment(id, null)).isTrue();

        assertThat(repository.getBookingAppointmentByEventId(id))
            .get()
            .extracting("eventSourceDesc")
            .isNull();

        repository.deleteBookingAppointment(id);

        assertThat(repository.getBookingAppointmentByEventId(id)).isEmpty();

        assertThat(repository.updateBookingAppointmentComment(id, "Don't care")).isFalse();
    }

    @Test
    public void testGetBookingIEPDetailsByBookingIds() {

        final List<Long> bookingIds = new ArrayList<>();
        bookingIds.add(-3L);

        final var IEPDetails = repository.getBookingIEPDetailsByBookingIds(bookingIds);
        assertThat(IEPDetails.get(-3L))
                .asList()
                .containsExactlyInAnyOrder(
                        PrivilegeDetail.builder()
                                .bookingId(-3L)
                                .iepDate(LocalDate.of(2017, 10, 12))
                                .iepTime(LocalDateTime.of(2017, 10, 12, 7, 53, 45))
                                .agencyId("LEI")
                                .iepLevel("Enhanced")
                                .userId("ITAG_USER")
                                .comments("Did not assault another inmate - data entry error.")
                                .build(),

                        PrivilegeDetail.builder()
                                .bookingId(-3L)
                                .iepDate(LocalDate.of(2017, 10, 12))
                                .iepTime(LocalDateTime.of(2017, 10, 12, 9, 44, 1))
                                .agencyId("LEI")
                                .iepLevel("Basic")
                                .userId("ITAG_USER")
                                .comments("Assaulted another inmate.")
                                .build(),

                        PrivilegeDetail.builder()
                                .bookingId(-3L)
                                .iepDate(LocalDate.of(2017, 8, 22))
                                .iepTime(LocalDateTime.of(2017, 8, 22, 18, 42, 35))
                                .agencyId("LEI")
                                .iepLevel("Standard")
                                .userId("ITAG_USER")
                                .comments("He has been a very good boy.")
                                .build(),

                        PrivilegeDetail.builder()
                                .bookingId(-3L)
                                .iepDate(LocalDate.of(2017, 7, 4))
                                .iepTime(LocalDateTime.of(2017, 7, 4, 12, 15, 42))
                                .agencyId("LEI")
                                .iepLevel("Entry")
                                .userId(null)
                                .comments(null)
                                .build()

                );
    }
}
