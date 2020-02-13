package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.ScheduleRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.support.InmateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class SchedulesServiceImplTest {

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

    private SchedulesService schedulesService;

    private final static LocalDate DATE = LocalDate.of(2018, Month.AUGUST, 31);
    private final static LocalDateTime TIME_1000 = LocalDateTime.of(DATE, LocalTime.of(10, 0));
    private final static LocalDateTime TIME_1040 = LocalDateTime.of(DATE, LocalTime.of(10, 40));
    private final static int MAX_BATCH_SIZE = 500;

    @BeforeEach
    public void init() {
        schedulesService = new SchedulesService(locationService, inmateService, bookingService, referenceDomainService, scheduleRepository, authenticationFacade, MAX_BATCH_SIZE);
        when(authenticationFacade.getCurrentUsername()).thenReturn("me");
    }

    @Test
    public void testGetLocationGroupEventsAM() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, null, null);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription", "eventType", "locationCode")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 11, 0), "Morning-11", "VISIT", null),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 9, 0), "Morning-12", "APP", null),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 10, 0), "Morning-10", null, null));
    }

    @Test
    public void testGetLocationGroupEventsOrder1() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, "lastName", Order.ASC);

        assertThat(results).asList().extracting("cellLocation", "lastName")
                .containsExactly(
                        tuple("H2", "Anderson"),
                        tuple("M0", "Bloggs"),
                        tuple("H1", "Zed"));
    }

    @Test
    public void testGetLocationGroupEventsOrder2() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, "cellLocation", Order.DESC);

        assertThat(results).asList().extracting("cellLocation", "lastName")
                .containsExactly(
                        tuple("M0", "Bloggs"),
                        tuple("H2", "Anderson"),
                        tuple("H1", "Zed"));
    }

    @Test
    public void testGetLocationGroupEventsOrder3() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, "lastName", Order.DESC);

        assertThat(results).asList().extracting("cellLocation", "lastName")
                .containsExactly(
                        tuple("H1", "Zed"),
                        tuple("M0", "Bloggs"),
                        tuple("H2", "Anderson"));
    }

    @Test
    public void testGetLocationGroupEventsOrder4() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.ED, "cellLocation", Order.ASC);

        assertThat(results).asList().extracting("cellLocation", "lastName")
                .containsExactly(
                        tuple("H1", "Zed"),
                        tuple("H2", "Anderson"),
                        tuple("M0", "Bloggs"),
                        tuple("M0", "Bloggs"),
                        tuple("M0", "InSameCell"));
    }

    @Test
    public void testGetLocationGroupEventsPM() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.PM, null, null);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 12, 0), "Afternoon-11"),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 14, 0), "Afternoon-12"));
    }

    @Test
    public void testGetLocationGroupEventsED() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.ED, null, null);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 17, 0), "Eve-11"),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 30), "Eve-12"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 0), "Eve1-10"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 30), "Eve2-10"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 19, 0), "Eve-13"));
    }

    @Test
    public void testGetLocationGroupEventsMapping() {
        setupGroupExpectations();
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, null, null);

        assertThat(results).asList()
                .contains(PrisonerSchedule.builder()
                        .cellLocation("M0")
                        .firstName("Joe")
                        .lastName("Bloggs")
                        .offenderNo("A10")
                        .comment("Fully populated event")
                        .startTime(TIME_1000)
                        .endTime(TIME_1040)
                        .event("APP sub type")
                        .eventDescription("Morning-10")
                        .bookingId(-10L)
                        .build()
                );
    }

    @Test
    public void testGetLocationGroupNoInmates() {
        final var results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM, null, null);

        assertThat(results).asList().hasSize(0);
    }

    private void setupGroupExpectations() {
        final var inmatesOnMyWing = Arrays.asList(
                InmateDto.builder().bookingId(-10L).offenderNo("A10").locationDescription("M0").firstName("Joe").lastName("Bloggs").build(),
                InmateDto.builder().bookingId(-11L).locationDescription("H1").lastName("Zed").build(),
                InmateDto.builder().bookingId(-12L).locationDescription("H2").lastName("Anderson").build(),
                InmateDto.builder().bookingId(-13L).offenderNo("B11").locationDescription("M0").firstName("Second").lastName("InSameCell").build()
        );
        when(inmateService.findInmatesByLocation("me",
                "LEI", Arrays.asList(-100L, -101L))).thenReturn(inmatesOnMyWing);

        // group 'myWing' consists of 2 locations:
        when(locationService.getCellLocationsForGroup("LEI", "myWing")).thenReturn(
                Arrays.asList(
                        Location.builder().locationId(-100L).build(),
                        Location.builder().locationId(-101L).build()
                ));

        final var complete = ScheduledEvent.builder()
                .bookingId(-10L)
                .startTime(TIME_1000)
                .endTime(TIME_1040)
                .eventSourceDesc("Fully populated event")
                .eventSubType("APP sub type")
                .eventSubTypeDesc("Morning-10")
                .build();
        final var eventsFor10 = Arrays.asList(
                complete,
                ScheduledEvent.builder().bookingId(-10L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 30))).eventSubTypeDesc("Eve2-10").eventType("PRISON_ACT").build(),
                ScheduledEvent.builder().bookingId(-10L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 0))).eventSubTypeDesc("Eve1-10").eventType("PRISON_ACT").build()
        );
        final var eventsFor11 = Arrays.asList(
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(12, 0))).eventSubTypeDesc("Afternoon-11").eventType("VISIT").build(),
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(17, 0))).eventSubTypeDesc("Eve-11").eventType("VISIT").build(),
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(11, 0))).eventSubTypeDesc("Morning-11").eventType("VISIT").build()
        );
        final var eventsFor12 = Arrays.asList(
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(9, 0))).eventSubTypeDesc("Morning-12").eventType("APP").build(),
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 30))).eventSubTypeDesc("Eve-12").eventType("APP").build(),
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(14, 0))).eventSubTypeDesc("Afternoon-12").eventType("APP").build()
        );
        final var eventsFor13 = Arrays.asList(
                ScheduledEvent.builder().bookingId(-13L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(19, 0))).eventSubTypeDesc("Eve-13").eventType("APP").build()
        );

        final var events = Stream.of(eventsFor10, eventsFor11, eventsFor12, eventsFor13).flatMap(Collection::stream).collect(Collectors.toList());
        when(bookingService.getEventsOnDay(ImmutableSet.copyOf(ImmutableList.of(-10L, -11L, -12L, -13L)), SchedulesServiceImplTest.DATE)).thenReturn(events);
    }

    @Test
    public void testGetLocationEventsAppAM() {
        final var app = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(TIME_1000)
                .event("APP")
                .build();
        final var apps = Arrays.asList(app);
        when(scheduleRepository.getLocationAppointments(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(apps);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "APP", DATE, TimeSlot.AM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    public void testGetLocationEventsVisitPM() {
        final var visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(14, 0)))
                .event("VISIT")
                .build();
        final var visits = Arrays.asList(visit);
        when(scheduleRepository.getLocationVisits(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(visits);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "VISIT", DATE, TimeSlot.PM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    public void testGetLocationEventsActivityED() {
        final var visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(21, 0)))
                .event("PROG")
                .build();
        final var visits = Arrays.asList(visit);
        when(scheduleRepository.getActivitiesAtLocation(-100L, DATE, DATE, "lastName", Order.ASC, false)).thenReturn(visits);

        final var results = schedulesService.getLocationEvents("LEI", -100L, "PROG", DATE, TimeSlot.ED, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    public void testScheduleEventIsCorrectlyMappedToPrisonSchedule() {
        final var today = LocalDate.now().atStartOfDay().toLocalDate();
        final var now = today.atStartOfDay().plusHours(10);

        when(authenticationFacade.getCurrentUsername()).thenReturn("username");
        when(inmateService.findInmatesByLocation(anyString(), anyString(), anyList())).thenReturn(List.of(
                InmateDto.builder()
                        .locationDescription("cell location")
                        .firstName("first name")
                        .lastName("last name")
                        .offenderNo("offenderNo")
                        .bookingId(1L)
                        .build()));

        when(bookingService.getEventsOnDay(anyCollection(), any(LocalDate.class)))
                .thenReturn(List.of(
                        ScheduledEvent
                                .builder()
                                .bookingId(1L)
                                .eventClass("event class")
                                .eventId(2L)
                                .eventStatus("event status")
                                .eventType("event type")
                                .eventTypeDesc("event type description")
                                .eventSubType("event sub type")
                                .eventSubTypeDesc("event sub type description")
                                .eventDate(today)
                                .startTime(now)
                                .endTime(now)
                                .eventLocation("event location")
                                .eventLocationId(3L)
                                .eventSource("event source")
                                .eventSourceCode("event source code")
                                .eventSourceDesc("event source description")
                                .eventOutcome("event out come")
                                .performance("performance")
                                .outcomeComment("comments")
                                .paid(false)
                                .payRate(BigDecimal.valueOf(1))
                                .build()
                        )
                );

        final var activities = schedulesService.getLocationGroupEvents("LEI", "Houseblock1",
                today, TimeSlot.AM, null, null);

        assertThat(activities).asList().containsSequence(
                PrisonerSchedule
                        .builder()
                        .firstName("first name")
                        .lastName("last name")
                        .startTime(now)
                        .endTime(now)
                        .cellLocation("cell location")
                        .locationId(3L)
                        .bookingId(1L)
                        .offenderNo("offenderNo")
                        .eventOutcome("event out come")
                        .eventType("event type")
                        .event("event sub type")
                        .eventLocationId(3L)
                        .eventLocation("Event Location")
                        .outcomeComment("comments")
                        .eventStatus("event status")
                        .eventId(2L)
                        .paid(false)
                        .payRate(BigDecimal.valueOf(1))
                        .performance("performance")
                        .eventType("event type")
                        .eventDescription("event sub type description")
                        .comment("event source description")
                        .build());
    }

    @Test
    public void testGeActivitiesAtAllLocations_callsTheRepositoryWithTheCorrectParameters() {
        final var today = LocalDate.now();
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, sortFields, Order.ASC);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", today, today, sortFields, Order.ASC);
    }

    @Test
    public void testGeActivitiesAtAllLocations_appliesTimeSlotFiltering() {
        final var today = LocalDate.now();

        when(scheduleRepository.getAllActivitiesAtAgency(eq("LEI"), eq(today), eq(today), eq("lastName"), eq(Order.ASC)))
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

        final var activities = schedulesService.getActivitiesAtAllLocations("LEI", today, null, TimeSlot.AM, null, Order.ASC);

        assertThat(activities).hasSize(1);
    }

    @Test
    public void testCallsToGetVisits_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getVisits("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getVisits(any(), anyList(), any());
    }

    @Test
    public void testCallsToGetAppointments_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getAppointments("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getAppointments(any(), anyList(), any());
    }


    @Test
    public void testCallsToGetActivities_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getActivities("LEI", offenders, LocalDate.now(), TimeSlot.AM, true);

        verify(scheduleRepository, times(2)).getActivities(any(), anyList(), any());
    }

    @Test
    public void testCallsToGetCourtEvents_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getCourtEvents("LEI", offenders, LocalDate.now(), TimeSlot.AM);

        verify(scheduleRepository, times(2)).getCourtEvents(anyList(), any());
    }

    @Test
    public void testCallsToGetExternalTransfers_AreBatched() {
        final var offenders = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());
        schedulesService.getExternalTransfers("LEI", offenders, LocalDate.now());

        verify(scheduleRepository, times(2)).getExternalTransfers(any(), anyList(), any());
    }

    @Test
    public void testGeActivitiesAtAllLocations_CallsTheRepositoryWithTheCorrectParameters() {
        final var from = LocalDate.now();
        final var to = LocalDate.now().plusDays(1);

        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", from, to, TimeSlot.AM, sortFields, Order.ASC);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, to, sortFields, Order.ASC);
    }

    @Test
    public void testGeActivitiesAtAllLocations_UseFromDate_WhenToDateIsNull() {
        final var from = LocalDate.now().plusDays(-10);
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtAllLocations("LEI", from, null, TimeSlot.AM, sortFields, Order.ASC);

        verify(scheduleRepository).getAllActivitiesAtAgency("LEI", from, from, sortFields, Order.ASC);
    }

    @Test
    public void testLocationIdIsValidated_OnGetLocationActivity() {
        final var locationId = -1L;

        schedulesService.getActivitiesAtLocation(locationId, LocalDate.now(), TimeSlot.AM, "", Order.ASC, false);

        verify(locationService).getLocation(locationId);
    }


    @Test
    public void testGetLocationActivity_callsTheRepositoryWithTheCorrectParameters() {
        final var locationId = -1L;
        final var today = LocalDate.now();
        final var sortFields = "lastName,startTime";

        schedulesService.getActivitiesAtLocation(locationId, today, TimeSlot.AM, sortFields, Order.ASC, true);

        verify(scheduleRepository).getActivitiesAtLocation(locationId, today, today, sortFields, Order.ASC, true);
    }

    @Test
    public void testGetLocationActivity_appliesTimeSlotFiltering() {
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

}
