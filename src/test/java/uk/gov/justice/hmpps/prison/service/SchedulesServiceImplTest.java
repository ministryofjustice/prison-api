package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.ScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivitiesCount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledActivityRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulesServiceImplTest {

    @Mock
    private LocationService locationService;

    @Mock
    private InmateService inmateService;

    @Mock
    private BookingService bookingService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private ScheduledActivityRepository scheduledActivityRepository;

    private SchedulesService schedulesService;

    private final static LocalDate DATE = LocalDate.of(2018, Month.AUGUST, 31);
    private final static LocalDateTime TIME_1000 = LocalDateTime.of(DATE, LocalTime.of(10, 0));
    private final static int MAX_BATCH_SIZE = 500;

    @BeforeEach
    void init() {
        schedulesService = new SchedulesService(
                locationService,
                inmateService,
                bookingService,
                referenceDomainService,
                scheduleRepository,
                authenticationFacade,
                scheduledActivityRepository,
                MAX_BATCH_SIZE
        );
    }

    @Test
    void testGetLocationEventsAppAM() {
        final var app = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(TIME_1000)
                .event("APP")
                .build();
        final var apps = List.of(app);
        when(scheduleRepository.getLocationAppointments(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(apps);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "APP", DATE, TimeSlot.AM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    void testGetLocationEventsVisitPM() {
        final var visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(14, 0)))
                .event("VISIT")
                .build();
        final var visits = List.of(visit);
        when(scheduleRepository.getLocationVisits(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(visits);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "VISIT", DATE, TimeSlot.PM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    void testGetLocationEventsActivityED() {
        final var visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(21, 0)))
                .event("PROG")
                .build();
        final var visits = List.of(visit);
        when(scheduleRepository.getActivitiesAtLocation(-100L, DATE, DATE, "lastName", Order.ASC, false)).thenReturn(visits);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "PROG", DATE, TimeSlot.ED, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Nested
    class getActivitiesAtAllLocations {
        @Test
        void testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
            final var today = LocalDate.now();
            final var sortFields = "lastName,startTime";

            schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, sortFields, Order.ASC, true);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", today, today, sortFields, Order.ASC, true, false);
        }

        @Test
        void testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
            final var today = LocalDate.now();

            when(scheduleRepository.getAllActivitiesAtAgency(eq("LEI"), eq(today), eq(today), eq("lastName"), eq(Order.ASC), eq(false), eq(false)))
                .thenReturn(List.of(
                    PrisonerSchedule
                        .builder()
                        .startTime(LocalDateTime.now().withHour(23))
                        .endTime(LocalDateTime.now().withHour(23))
                        .locationId(3L)
                        .bookingId(1L)
                        .eventLocationId(3L)
                        .eventId(2L)
                        .build(),
                    PrisonerSchedule
                        .builder()
                        .startTime(LocalDateTime.now().withHour(11))
                        .endTime(LocalDateTime.now().withHour(11))
                        .locationId(3L)
                        .bookingId(1L)
                        .eventLocationId(3L)
                        .eventId(3L)
                        .build()
                ));

            final var activities = schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, null, Order.ASC, false);

            assertThat(activities).hasSize(1);
        }

        @Test
        void testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
            final var from = LocalDate.now();
            final var to = LocalDate.now().plusDays(1);

            final var sortFields = "lastName,startTime";

            schedulesService.getActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM, sortFields, Order.ASC, false);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, to, sortFields, Order.ASC, false, false);
        }

        @Test
        void testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
            final var from = LocalDate.now().plusDays(-10);
            final var sortFields = "lastName,startTime";

            schedulesService.getActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM, sortFields, Order.ASC, false);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, from, sortFields, Order.ASC, false, false);
        }
    }

    @Nested
    class getSuspendedActivitiesAtAllLocations {
        @Test
        void testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
            final var today = LocalDate.now();
            final var sortFields = "lastName";

            schedulesService.getSuspendedActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", today, today, sortFields, Order.ASC, true, true);
        }

        @Test
        void testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
            final var today = LocalDate.now();

            when(scheduleRepository.getAllActivitiesAtAgency(eq("LEI"), eq(today), eq(today), eq("lastName"), eq(Order.ASC), eq(true), eq(true)))
                .thenReturn(List.of(
                    PrisonerSchedule
                        .builder()
                        .startTime(LocalDateTime.now().withHour(23))
                        .endTime(LocalDateTime.now().withHour(23))
                        .locationId(3L)
                        .bookingId(1L)
                        .eventLocationId(3L)
                        .eventId(2L)
                        .build(),
                    PrisonerSchedule
                        .builder()
                        .startTime(LocalDateTime.now().withHour(11))
                        .endTime(LocalDateTime.now().withHour(11))
                        .locationId(3L)
                        .bookingId(1L)
                        .eventLocationId(3L)
                        .eventId(3L)
                        .build()
                ));

            final var activities = schedulesService.getSuspendedActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM);

            assertThat(activities).hasSize(1);
        }

        @Test
        void testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
            final var from = LocalDate.now();
            final var to = LocalDate.now().plusDays(1);

            final var sortFields = "lastName";

            schedulesService.getSuspendedActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, to, sortFields, Order.ASC, true, true);
        }

        @Test
        void testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
            final var from = LocalDate.now().plusDays(-10);
            final var sortFields = "lastName";

            schedulesService.getSuspendedActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM);

            verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, from, sortFields, Order.ASC, true, true);
        }
    }

    @Test
    void testCallsToGetVisits_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).toList();
        schedulesService.getVisits("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getVisits(any(), anyList(), any());
    }

    @Test
    void testCallsToGetAppointments_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).toList();
        schedulesService.getAppointments("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getAppointments(any(), anyList(), any());
    }


    @Test
    void testCallsToGetActivities_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).toList();
        schedulesService.getActivitiesByEventIds("LEI", offenders, LocalDate.now(), TimeSlot.AM, true);

        verify(scheduleRepository, times(2)).getActivities(any(), anyList(), any());
    }

    @Test
    void testCallsToGetCourtEvents_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).toList();
        schedulesService.getCourtEvents("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getCourtEvents(anyList(), any());
    }

    @Test
    void testCallsToGetExternalTransfers_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).toList();
        schedulesService.getExternalTransfers("LEI", offenders, LocalDate.now());

        verify(scheduleRepository, times(2)).getExternalTransfers(any(), anyList(), any());
    }

    @Test
    void testLocationIdIsValidated_OnGetLocationActivity() {
        final var locationId = -1L;

        schedulesService.getActivitiesAtLocation(locationId, LocalDate.now(), TimeSlot.AM, "", Order.ASC, false);

        verify(locationService).getLocation(locationId);
    }


    @Test
    void testGetLocationActivity_callsTheRepositoryWithTheCorrectParameters() {
        final var locationId = -1L;
        final var today = LocalDate.now();
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtLocation(locationId, today, TimeSlot.AM, sortFields, Order.ASC, true);

        verify(scheduleRepository).getActivitiesAtLocation(locationId, today, today, sortFields, Order.ASC, true);
    }

    @Test
    void testGetLocationActivity_appliesTimeSlotFiltering() {
        final var today = LocalDate.now();

        when(scheduleRepository.getActivitiesAtLocation(anyLong(), any(), any(), anyString(), any(), anyBoolean()))
                .thenReturn(List.of(
                        PrisonerSchedule
                                .builder()
                                .startTime(LocalDateTime.now().withHour(23))
                                .endTime(LocalDateTime.now().withHour(23))
                                .build(),
                        PrisonerSchedule
                                .builder()
                                .startTime(LocalDateTime.now().withHour(11))
                                .endTime(LocalDateTime.now().withHour(11))
                                .build()
                ));

        final var activities = schedulesService
                .getActivitiesAtLocation(1L, today, TimeSlot.AM, null, Order.ASC, false);

        assertThat(activities).hasSize(1);
    }

    @Test
    void testBatchGetScheduledActivities() {
        when(scheduledActivityRepository.findAllByEventIdIn(any())).thenReturn(Collections.emptyList());

        final var eventIds = IntStream.range(1, 1000).mapToObj(Long::valueOf).toList();

        schedulesService.getActivitiesByEventIds("LEI", eventIds);

        verify(scheduledActivityRepository,  times(2)).findAllByEventIdIn(any());
    }

    @Test
    void getCountActivities() {
        when(scheduledActivityRepository.getActivities(any(), any(), any())).thenReturn(List.of(
            new PrisonerActivityImpl(-1L, "Y", "ALLOC", null, null, "2022-02-23T10:20:30"),
            // null is not suspended
            new PrisonerActivityImpl(-1L, null, "ALLOC", null, null, "2022-02-23T10:20:30"),
            new PrisonerActivityImpl(-2L, "N", "ALLOC", null,null, "2022-02-23T10:20:30"),
            // afternoon slot so won't be counted
            new PrisonerActivityImpl(-3L, "N", "ALLOC", null,null, "2022-02-23T12:00:00"),
            // evening slot so will be counted
            new PrisonerActivityImpl(-1L, "N", "ALLOC", null,null, "2022-02-23T17:05:00"),
            // end date of program same as schedule date so will be counted
            new PrisonerActivityImpl(-4L, "N", "END", "2022-03-05", "2022-03-05", "2022-02-23T10:20:30"),
            // end date of program after schedule date so will be counted
            new PrisonerActivityImpl(-5L, "N", "END", "2022-03-26", "2022-03-05", "2022-02-23T10:20:30"),
            // end date of program before schedule date so won't be counted
            new PrisonerActivityImpl(-6L, "N", "END", "2022-03-04", "2022-03-05", "2022-02-23T10:20:30")
        ));
        final var startDate = LocalDate.parse("2022-02-23");
        final var endDate = LocalDate.parse("2022-04-23");
        final var counts = schedulesService.getCountActivities("MDI", startDate, endDate, Set.of(TimeSlot.AM, TimeSlot.ED), Map.of());
        assertThat(counts).isEqualTo(new PrisonerActivitiesCount(6, 1, 6));
        verify(scheduledActivityRepository).getActivities("MDI", startDate, endDate);
    }

    @Test
    void getCountActivities_notRecorded() {
        when(scheduledActivityRepository.getActivities(any(), any(), any())).thenReturn(List.of(
            new PrisonerActivityImpl(-1L, "Y", "END", "2022-02-23", "2022-02-20", "2022-02-23T10:20:30"),
            // null is not suspended
            new PrisonerActivityImpl(-1L, null, "ALLOC", null, null, "2022-02-23T10:20:30"),
            new PrisonerActivityImpl(-2L, "N", "ALLOC", "2022-02-23", null, "2022-02-23T10:20:30"),
            // afternoon slot so won't be counted
            new PrisonerActivityImpl(-3L, "N", "ALLOC", "2022-02-23", null,"2022-02-23T12:00:00"),
            // evening slot so should be counted
            new PrisonerActivityImpl(-4L, "N", "ALLOC", "2022-02-23", null,"2022-02-23T17:05:00")
        ));
        final var startDate = LocalDate.parse("2022-02-23");
        final var endDate = LocalDate.parse("2022-04-23");
        final var attendances = Map.of(
            -1L, 2L, // booking -1 has 2 scheduled attendances so will cancel each other out
            -4L, 1L, // booking -4 has 1 scheduled attendance
            -5L, 10L); // booking -5 doesn't have any scheduled attendances so will be ignored
        final var counts = schedulesService.getCountActivities("MDI", startDate, endDate, Set.of(TimeSlot.AM, TimeSlot.ED), attendances);
        assertThat(counts).isEqualTo(new PrisonerActivitiesCount(4, 1, 1));
        verify(scheduledActivityRepository).getActivities("MDI", startDate, endDate);
    }


    @Test
    void testGetScheduledTransfersForPrisoner() {
        final var transfer = PrisonerSchedule.builder()
            .offenderNo("A10")
            .startTime(TIME_1000)
            .event("28")
            .eventStatus("SCH")
            .build();
        final var transfers = List.of(transfer);
        when(scheduleRepository.getScheduledTransfersForPrisoner("A10")).thenReturn(transfers);

        final var results = schedulesService.getScheduledTransfersForPrisoner("A10");
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }
}
