package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.*;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class AppointmentsServiceImplTest {
    private static final String USERNAME = "username";
    private static final String BULK_APPOINTMENTS_ROLE = "BULK_APPOINTMENTS";
    private static final Authentication AUTHENTICATION_NO_ROLES = new TestingAuthenticationToken(USERNAME, null);

    private static final Location LOCATION_A = Location.builder().locationId(0L).agencyId("A").build();
    private static final Location LOCATION_B = Location.builder().locationId(1L).agencyId("B").build();
    private static final Location LOCATION_C = Location.builder().locationId(2L).agencyId("C").build();

    private static final AppointmentDetails DETAILS_1 = AppointmentDetails.builder().bookingId(1L).build();

    private static final AppointmentDetails DETAILS_2 = AppointmentDetails
            .builder()
            .bookingId(2L)
            .startTime(LocalDateTime.of(2018, 2, 27, 13, 30)) // Tuesday
            .endTime(LocalDateTime.of(2018, 2, 27, 13, 50))
            .build();

    private static final AppointmentDetails DETAILS_3 = AppointmentDetails
            .builder()
            .bookingId(2L)
            .startTime(LocalDateTime.of(2018, 2, 27, 13, 30)) // Tuesday
            .build();

    private static final ReferenceCode REFERENCE_CODE_T = ReferenceCode
            .builder()
            .activeFlag("Y")
            .code("T")
            .domain(ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain())
            .build();


    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    @Mock
    private TelemetryClient telemetryClient;

    private AppointmentsServiceImpl appointmentsService;

    @Before
    public void initMocks() {
        SecurityContextHolder.createEmptyContext();
        ensureRoles(BULK_APPOINTMENTS_ROLE);
        MockitoAnnotations.initMocks(this);
        appointmentsService = new AppointmentsServiceImpl(
                bookingRepository,
                new AuthenticationFacade(),
                locationService,
                referenceDomainService,
                telemetryClient
        );
    }

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void createTooManyAppointments() {
        assertThatThrownBy(() ->
                appointmentsService.createAppointments(AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(
                                AppointmentDefaults
                                        .builder()
                                        .build())
                        .appointments(Arrays.asList(new AppointmentDetails[1001]))
                        .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Request to create 1001 appointments exceeds limit of 1000");

        verifyNoMoreInteractions(telemetryClient);
    }

    private void ensureRoles(final String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(USERNAME, null, roles));
    }

    @Test
    public void locationNotInCaseload() {
        stubLocation(LOCATION_C);

        assertThatThrownBy(() ->
                appointmentsService.createAppointments(
                        AppointmentsToCreate
                                .builder()
                                .appointmentDefaults(
                                        AppointmentDefaults
                                                .builder()
                                                .locationId(LOCATION_C.getLocationId())
                                                .build())
                                .appointments(List.of())
                                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Location does not exist or is not in your caseload.");

        verifyNoMoreInteractions(telemetryClient);
    }

    @Test
    public void skipLocationNotInCaseloadCheck() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        ensureRoles("GLOBAL_APPOINTMENT");

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1L))
                                .build())
                .appointments(List.of(DETAILS_1))
                .repeat(Repeat
                        .builder()
                        .count(13)
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .build())
                .build();

        appointmentsService.createAppointments(appointmentsToCreate);

        verify(bookingRepository)
                .createMultipleAppointments(
                        appointmentsToCreate.withDefaults(),
                        appointmentsToCreate.getAppointmentDefaults(),
                        LOCATION_B.getAgencyId());

        verify(telemetryClient).trackEvent(eq("AppointmentsCreated"), anyMap(), isNull());
    }

    @Test
    public void unknownAppointmentType() {
        stubLocation(LOCATION_B);

        assertThatThrownBy(() ->
                appointmentsService.createAppointments(
                        AppointmentsToCreate
                                .builder()
                                .appointmentDefaults(
                                        AppointmentDefaults
                                                .builder()
                                                .locationId(LOCATION_B.getLocationId())
                                                .appointmentType("NOT_KNOWN")
                                                .startTime(LocalDateTime.now())
                                                .build())
                                .appointments(List.of())
                                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Event type not recognised.");

        verifyNoMoreInteractions(telemetryClient);
    }

    @Test
    public void createNoAppointments() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);

        appointmentsService.createAppointments(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(
                                AppointmentDefaults
                                        .builder()
                                        .locationId(LOCATION_B.getLocationId())
                                        .appointmentType(REFERENCE_CODE_T.getCode())
                                        .startTime(LocalDateTime.now())
                                        .build())
                        .appointments(List.of())
                        .build());

        verifyNoMoreInteractions(telemetryClient);
    }

    @Test
    public void bookingIdNotInCaseload() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);

        assertThatThrownBy(() ->
                appointmentsService.createAppointments(
                        AppointmentsToCreate
                                .builder()
                                .appointmentDefaults(
                                        AppointmentDefaults
                                                .builder()
                                                .locationId(LOCATION_B.getLocationId())
                                                .appointmentType(REFERENCE_CODE_T.getCode())
                                                .startTime(LocalDateTime.now().plusHours(1))
                                                .build())
                                .appointments(List.of(DETAILS_1))
                                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A BookingId does not exist in your caseload");

        verifyNoMoreInteractions(telemetryClient);
    }

    @Test
    public void bookingIdInCaseload() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1))
                                .build())
                .appointments(List.of(DETAILS_1))
                .build();

        appointmentsService.createAppointments(appointmentsToCreate);

        verify(bookingRepository)
                .createMultipleAppointments(
                        appointmentsToCreate.withDefaults(),
                        appointmentsToCreate.getAppointmentDefaults(),
                        LOCATION_B.getAgencyId());

        verify(telemetryClient).trackEvent(eq("AppointmentsCreated"), anyMap(), isNull());
    }

    @Test
    public void appointmentStartTimeTooLate() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1L))
                                .build())
                .appointments(List.of(DETAILS_1))
                .repeat(Repeat
                        .builder()
                        .count(13)
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .build())
                .build();

        assertThatThrownBy(() -> appointmentsService.createAppointments(appointmentsToCreate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("An appointment startTime is later than the limit of ");
    }

    @Test
    public void appointmentEndTimeTooLate() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1L))
                                .endTime(LocalDateTime.now().plusDays(31L).plusHours(1L))
                                .build())
                .appointments(List.of(DETAILS_1))
                .repeat(Repeat
                        .builder()
                        .count(12)
                        .repeatPeriod(RepeatPeriod.MONTHLY)
                        .build())
                .build();

        assertThatThrownBy(() -> appointmentsService.createAppointments(appointmentsToCreate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageStartingWith("An appointment endTime is later than the limit of ");
    }

    @Test
    public void rejectEndTimeBeforeStartTime() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(2L))
                                .endTime(LocalDateTime.now().plusHours(1L))
                                .build())
                .appointments(List.of(DETAILS_1))
                .build();

        assertThatThrownBy(() -> appointmentsService.createAppointments(appointmentsToCreate))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Appointment end time is before the start time.");
    }

    /**
     * Also shows that BULK_APPOINTMENTS role is not required when creating appointments for a single offender.
     */
    @Test
    public void acceptEndTimeSameAsStartTime() {
        ensureRoles(); // No roles
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId());

        final var in1Hour = LocalDateTime.now().plusHours(1);

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(in1Hour)
                                .endTime(in1Hour)
                                .build())
                .appointments(List.of(DETAILS_1))
                .build();

        appointmentsService.createAppointments(appointmentsToCreate);

        verify(bookingRepository)
                .createMultipleAppointments(
                        appointmentsToCreate.withDefaults(),
                        appointmentsToCreate.getAppointmentDefaults(),
                        LOCATION_B.getAgencyId());
    }

    @Test
    public void rejectMultipleOffendersWithoutBulkAppointmentRole() {
        ensureRoles(); // No roles
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId(), DETAILS_2.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1))
                                .build())
                .appointments(List.of(DETAILS_1, DETAILS_2))
                .build();

        assertThatThrownBy(() -> appointmentsService.createAppointments(appointmentsToCreate))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.");
    }

    @Test
    public void permitMultipleOffendersWithBulkAppointmentRole() {
        stubLocation(LOCATION_B);
        stubValidReferenceCode(REFERENCE_CODE_T);
        stubValidBookingIds(LOCATION_B.getAgencyId(), DETAILS_1.getBookingId(), DETAILS_2.getBookingId());

        final var appointmentsToCreate = AppointmentsToCreate
                .builder()
                .appointmentDefaults(
                        AppointmentDefaults
                                .builder()
                                .locationId(LOCATION_B.getLocationId())
                                .appointmentType(REFERENCE_CODE_T.getCode())
                                .startTime(LocalDateTime.now().plusHours(1))
                                .build())
                .appointments(List.of(DETAILS_1, DETAILS_2))
                .build();

        appointmentsService.createAppointments(appointmentsToCreate);

        verify(bookingRepository)
                .createMultipleAppointments(
                        appointmentsToCreate.withDefaults(),
                        appointmentsToCreate.getAppointmentDefaults(),
                        LOCATION_B.getAgencyId());
    }


    @Test
    public void shouldHandleNoRepeats() {
        assertThat(AppointmentsServiceImpl.withRepeats(null, Collections.singletonList(DETAILS_2))).containsExactly(DETAILS_2);
    }

    @Test
    public void shouldHandleOneRepeat() {
        assertThat(AppointmentsServiceImpl.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(1).build(),
                List.of(DETAILS_2)
        ))
                .containsExactly(DETAILS_2);
    }

    @Test
    public void shouldHandleMultipleRepeats() {
        assertThat(AppointmentsServiceImpl.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(3).build(),
                List.of(DETAILS_2)
        ))
                .containsExactly(
                        DETAILS_2,
                        DETAILS_2.toBuilder().startTime(DETAILS_2.getStartTime().plusDays(1)).endTime(DETAILS_2.getEndTime().plusDays(1)).build(),
                        DETAILS_2.toBuilder().startTime(DETAILS_2.getStartTime().plusDays(2)).endTime(DETAILS_2.getEndTime().plusDays(2)).build()
                );
    }

    @Test
    public void shouldHandleNullEndTime() {
        assertThat(AppointmentsServiceImpl.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(2).build(),
                List.of(DETAILS_3)
        ))
                .containsExactly(
                        DETAILS_3,
                        DETAILS_3.toBuilder().startTime(DETAILS_3.getStartTime().plusDays(1)).build()
                );
    }

    @Test
    public void shouldRepeatMultipleAppointments() {
        assertThat(AppointmentsServiceImpl.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(3).build(),
                List.of(DETAILS_2, DETAILS_3)
        ))
                .containsExactly(
                        DETAILS_2,
                        DETAILS_2.toBuilder().startTime(DETAILS_2.getStartTime().plusDays(1)).endTime(DETAILS_2.getEndTime().plusDays(1)).build(),
                        DETAILS_2.toBuilder().startTime(DETAILS_2.getStartTime().plusDays(2)).endTime(DETAILS_2.getEndTime().plusDays(2)).build(),
                        DETAILS_3,
                        DETAILS_3.toBuilder().startTime(DETAILS_3.getStartTime().plusDays(1)).build(),
                        DETAILS_3.toBuilder().startTime(DETAILS_3.getStartTime().plusDays(2)).build()
                );
    }

    @Test
    public void testCreateBookingAppointment() {

        final var appointmentType = "MEDE";
        final var locationId = -20L;
        final var bookingId = 100L;
        final var agencyId = "LEI";
        final var eventId = -10L;
        final var principal = "ME";
        final var expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build();
        final var location = Location.builder().locationId(locationId).agencyId(agencyId).build();

        final var newAppointment = NewAppointment.builder()
                .appointmentType(appointmentType)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .comment("comment")
                .locationId(locationId).build();

        when(locationService.getLocation(newAppointment.getLocationId())).thenReturn(location);
        when(locationService.getUserLocations(principal)).thenReturn(Collections.singletonList(location));

        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()));

        when(bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId))
                .thenReturn(eventId);

        when(bookingRepository.getBookingAppointment(bookingId, eventId)).thenReturn(expectedEvent);
        final var actualEvent = appointmentsService.createBookingAppointment(bookingId, principal, newAppointment);

        assertThat(actualEvent).isEqualTo(expectedEvent);
    }

    @Test
    public void testCreateBookingAppointmentInvalidStartTime() {

        final var bookingId = 100L;
        final var principal = "ME";

        final var newAppointment = NewAppointment.builder().startTime(LocalDateTime.now().plusDays(-1))
                .endTime(LocalDateTime.now().plusDays(2)).build();

        try {
            appointmentsService.createBookingAppointment(bookingId, principal, newAppointment);
            fail("Should have thrown exception");
        } catch (final BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("Appointment time is in the past.");
        }
    }

    @Test
    public void testCreateBookingAppointmentInvalidEndTime() {

        final var bookingId = 100L;
        final var principal = "ME";

        final var newAppointment = NewAppointment.builder().startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(1)).build();

        try {
            appointmentsService.createBookingAppointment(bookingId, principal, newAppointment);
            fail("Should have thrown exception");
        } catch (final BadRequestException e) {
            assertThat(e.getMessage()).isEqualTo("Appointment end time is before the start time.");
        }
    }

    @Test
    public void testCreateBookingAppointmentInvalidLocation() {

        final var appointmentType = "MEDE";
        final var locationId = -20L;
        final var bookingId = 100L;
        final var agencyId = "LEI";
        final var eventId = -10L;
        final var principal = "ME";
        final var expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build();
        final var location = Location.builder().locationId(locationId).agencyId(agencyId).build();

        final var newAppointment = NewAppointment.builder()
                .appointmentType(appointmentType)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .comment("comment")
                .locationId(locationId).build();

        when(locationService.getLocation(newAppointment.getLocationId())).thenReturn(location);
        when(locationService.getUserLocations(principal)).thenReturn(Collections.singletonList(location));

        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()));

        when(bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId))
                .thenReturn(eventId);

        when(bookingRepository.getBookingAppointment(bookingId, eventId)).thenReturn(expectedEvent);

        when(locationService.getLocation(newAppointment.getLocationId()))
                .thenThrow(new EntityNotFoundException("test"));

        assertThatThrownBy(() -> appointmentsService.createBookingAppointment(bookingId, principal, newAppointment))
                .isInstanceOf(BadRequestException.class).hasMessage("Location does not exist or is not in your caseload.");
    }

    @Test
    public void testCreateBookingAppointmentInvalidAppointmentType() {

        final var appointmentType = "MEDE";
        final var locationId = -20L;
        final var bookingId = 100L;
        final var agencyId = "LEI";
        final var eventId = -10L;
        final var principal = "ME";
        final var expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build();
        final var location = Location.builder().locationId(locationId).agencyId(agencyId).build();

        final var newAppointment = NewAppointment.builder().appointmentType(appointmentType)
                .startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(2)).comment("comment")
                .locationId(locationId).build();

        when(locationService.getLocation(newAppointment.getLocationId())).thenReturn(location);
        when(locationService.getUserLocations(principal)).thenReturn(Collections.singletonList(location));

        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()));

        when(bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId))
                .thenReturn(eventId);

        when(bookingRepository.getBookingAppointment(bookingId, eventId)).thenReturn(expectedEvent);

        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentsService.createBookingAppointment(bookingId, principal, newAppointment))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Event type not recognised.");
    }

    @Test
    public void testOverrideAgencyLocationTest() {
        final var appointmentType = "MEDE";
        final var locationId = -20L;
        final var bookingId = 100L;
        final var agencyId = "LEI";
        final var eventId = -10L;
        final var principal = "ME";
        final var expectedEvent = ScheduledEvent.builder().bookingId(bookingId).build();
        final var location = Location.builder().locationId(locationId).agencyId(agencyId).build();

        final var newAppointment = NewAppointment.builder()
                .appointmentType(appointmentType)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .comment("comment")
                .locationId(locationId).build();

        ensureRoles("GLOBAL_APPOINTMENT");

        when(locationService.getLocation(newAppointment.getLocationId())).thenReturn(location);
        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.of(ReferenceCode.builder().code(appointmentType).build()));

        when(bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId))
                .thenReturn(eventId);

        when(bookingRepository.getBookingAppointment(bookingId, eventId)).thenReturn(expectedEvent);

        appointmentsService.createBookingAppointment(bookingId, principal, newAppointment);

        verify(locationService, never()).getUserLocations(principal);
    }

    private void stubValidBookingIds(final String agencyId, final long... bookingIds) {
        final var ids = Arrays.stream(bookingIds).boxed().collect(Collectors.toList());
        when(bookingRepository.findBookingsIdsInAgency(ids, agencyId)).thenReturn(ids);
    }


    private void stubValidReferenceCode(final ReferenceCode code) {
        when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(),
                code.getCode(),
                false))
                .thenReturn(Optional.of(REFERENCE_CODE_T));
    }

    private void stubLocation(final Location location) {
        when(locationService.getUserLocations(anyString())).thenReturn(List.of(LOCATION_A, LOCATION_B));
        when(locationService.getLocation(location.getLocationId())).thenReturn(location);
    }
}
