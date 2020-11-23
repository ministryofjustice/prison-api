package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.ScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledActivityRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
    private final static LocalDateTime TIME_1040 = LocalDateTime.of(DATE, LocalTime.of(10, 40));
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

    @Test
    void testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
        final var today = LocalDate.now();
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, sortFields, Order.ASC, true);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", today, today, sortFields, Order.ASC, true);
    }

    @Test
    void testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
        final var today = LocalDate.now();

        when(scheduleRepository.getAllActivitiesAtAgency(eq("LEI"), eq(today), eq(today), eq("lastName"), eq(Order.ASC), eq(false)))
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
    void testCallsToGetVisits_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getVisits("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getVisits(any(), anyList(), any());
    }

    @Test
    void testCallsToGetAppointments_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getAppointments("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getAppointments(any(), anyList(), any());
    }


    @Test
    void testCallsToGetActivities_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getActivitiesByEventIds("LEI", offenders, LocalDate.now(), TimeSlot.AM, true);

        verify(scheduleRepository, times(2)).getActivities(any(), anyList(), any());
    }

    @Test
    void testCallsToGetCourtEvents_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getCourtEvents("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getCourtEvents(anyList(), any());
    }

    @Test
    void testCallsToGetExternalTransfers_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getExternalTransfers("LEI", offenders, LocalDate.now());

        verify(scheduleRepository, times(2)).getExternalTransfers(any(), anyList(), any());
    }

    @Test
    void testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
        final var from = LocalDate.now();
        final var to = LocalDate.now().plusDays(1);

        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM, sortFields, Order.ASC, false);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, to, sortFields, Order.ASC, false);
    }

    @Test
    void testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
        final var from = LocalDate.now().plusDays(-10);
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM, sortFields, Order.ASC, false);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, from, sortFields, Order.ASC, false);
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

        final var eventIds = IntStream.range(1, 1000).mapToObj(Long::valueOf).collect(Collectors.toList());

        schedulesService.getActivitiesByEventIds("LEI", eventIds);

        verify(scheduledActivityRepository,  times(2)).findAllByEventIdIn(any());
    }
}
