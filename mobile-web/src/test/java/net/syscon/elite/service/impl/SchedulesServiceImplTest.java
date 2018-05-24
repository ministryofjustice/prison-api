package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.ScheduleRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.InmateDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void init() {
        schedulesService = new SchedulesServiceImpl(locationService, inmateService, bookingService, referenceDomainService, scheduleRepository, authenticationFacade);
        when(authenticationFacade.getCurrentUsername()).thenReturn("me");
    }

    @Test
    public void testGetLocationGroupEventsAM() {
        setupGroupExpectations();
        List<PrisonerSchedule> results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 11, 0), "Morning-11"),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 9, 0), "Morning-12"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 10, 0), "Morning-10"));
    }

    @Test
    public void testGetLocationGroupEventsPM() {
        setupGroupExpectations();
        List<PrisonerSchedule> results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.PM);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 12, 0), "Afternoon-11"),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 14, 0), "Afternoon-12"));
    }

    @Test
    public void testGetLocationGroupEventsED() {
        setupGroupExpectations();
        List<PrisonerSchedule> results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.ED);

        assertThat(results).asList().extracting("cellLocation", "startTime", "eventDescription")
                .containsExactly(
                        tuple("H1", LocalDateTime.of(2018, Month.AUGUST, 31, 17, 0), "Eve-11"),
                        tuple("H2", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 30), "Eve-12"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 0), "Eve1-10"),
                        tuple("M0", LocalDateTime.of(2018, Month.AUGUST, 31, 18, 30), "Eve2-10"));
    }

    @Test
    public void testGetLocationGroupEventsMapping() {
        setupGroupExpectations();
        List<PrisonerSchedule> results = schedulesService.getLocationGroupEvents("LEI", "myWing",
                DATE, TimeSlot.AM);

        assertThat(results).asList()
                .contains(PrisonerSchedule.builder()
                        .cellLocation("M0")
                        .firstName("Joe")
                        .lastName("Bloggs")
                        .offenderNo("A10")
                        .comment("Fully populated event")
                        .startTime(TIME_1000)
                        .endTime(TIME_1040)
                        .event("APP")
                        .eventDescription("Morning-10")
                        .build()
                );
    }

    private void setupGroupExpectations() {
        final List<InmateDto> inmatesOnMyWing = Arrays.asList(
                InmateDto.builder().bookingId(-10L).offenderNo("A10").locationDescription("M0").firstName("Joe").lastName("Bloggs").build(),
                InmateDto.builder().bookingId(-11L).locationDescription("H1").build(),
                InmateDto.builder().bookingId(-12L).locationDescription("H2").build()
        );
        when(inmateService.findInmatesByLocation("me",
                "LEI", Arrays.asList(-100L, -101L))).thenReturn(inmatesOnMyWing);

        // group 'myWing' consists of 2 locations:
        when(locationService.getGroup("LEI", "myWing")).thenReturn(
                Arrays.asList(
                        Location.builder().locationId(-100L).build(),
                        Location.builder().locationId(-101L).build()
                ));

        final ScheduledEvent complete = ScheduledEvent.builder()
                .bookingId(-10L)
                .startTime(TIME_1000)
                .endTime(TIME_1040)
                .eventSourceDesc("Fully populated event")
                .eventSubType("APP")
                .eventSubTypeDesc("Morning-10")
                .build();
        List<ScheduledEvent> eventsFor10 = Arrays.asList(
                complete,
                ScheduledEvent.builder().bookingId(-10L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 30))).eventSubTypeDesc("Eve2-10").build(),
                ScheduledEvent.builder().bookingId(-10L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 0))).eventSubTypeDesc("Eve1-10").build()
        );
        List<ScheduledEvent> eventsFor11 = Arrays.asList(
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(12, 0))).eventSubTypeDesc("Afternoon-11").build(),
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(17, 0))).eventSubTypeDesc("Eve-11").build(),
                ScheduledEvent.builder().bookingId(-11L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(11, 0))).eventSubTypeDesc("Morning-11").build()
        );
        List<ScheduledEvent> eventsFor12 = Arrays.asList(
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(9, 0))).eventSubTypeDesc("Morning-12").build(),
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(18, 30))).eventSubTypeDesc("Eve-12").build(),
                ScheduledEvent.builder().bookingId(-12L).startTime(LocalDateTime.of(SchedulesServiceImplTest.DATE, LocalTime.of(14, 0))).eventSubTypeDesc("Afternoon-12").build()
        );
        when(bookingService.getEventsOnDay(-10L, SchedulesServiceImplTest.DATE)).thenReturn(eventsFor10);
        when(bookingService.getEventsOnDay(-11L, SchedulesServiceImplTest.DATE)).thenReturn(eventsFor11);
        when(bookingService.getEventsOnDay(-12L, SchedulesServiceImplTest.DATE)).thenReturn(eventsFor12);
    }

    @Test
    public void testGetLocationEventsAppAM() {
        final PrisonerSchedule app = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(TIME_1000)
                .event("APP")
                .build();
        List<PrisonerSchedule> apps = Arrays.asList(app);
        when(scheduleRepository.getLocationAppointments(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(apps);

        List<PrisonerSchedule> results = schedulesService.getLocationEvents("LEI", -100L, "APP", DATE, TimeSlot.AM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    public void testGetLocationEventsVisitPM() {
        final PrisonerSchedule visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(14, 0)))
                .event("VISIT")
                .build();
        List<PrisonerSchedule> visits = Arrays.asList(visit);
        when(scheduleRepository.getLocationVisits(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(visits);

        List<PrisonerSchedule> results = schedulesService.getLocationEvents("LEI", -100L, "VISIT", DATE, TimeSlot.PM, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }

    @Test
    public void testGetLocationEventsActivityED() {
        final PrisonerSchedule visit = PrisonerSchedule.builder()
                .cellLocation("M0")
                .offenderNo("A10")
                .startTime(LocalDateTime.of(DATE, LocalTime.of(21, 0)))
                .event("PROG")
                .build();
        List<PrisonerSchedule> visits = Arrays.asList(visit);
        when(scheduleRepository.getLocationActivities(-100L, DATE, DATE, "lastName", Order.ASC)).thenReturn(visits);

        List<PrisonerSchedule> results = schedulesService.getLocationEvents("LEI", -100L, "PROG", DATE, TimeSlot.ED, null, null);
        assertThat(results.get(0).getOffenderNo()).isEqualTo("A10");
    }
}
