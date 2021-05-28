package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ScheduledAppointmentDto;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.CreatedAppointmentDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.Repeat;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.RepeatPeriod;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledAppointmentRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AppointmentsServiceImplTest {
    private static final String USERNAME = "username";
    private static final String BULK_APPOINTMENTS_ROLE = "BULK_APPOINTMENTS";

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
    private ScheduledAppointmentRepository scheduledAppointmentRepository;

    @Mock
    private TelemetryClient telemetryClient;

    private AppointmentsService appointmentsService;

    @BeforeEach
    public void initMocks() {
        SecurityContextHolder.createEmptyContext();
        ensureRoles(BULK_APPOINTMENTS_ROLE);
        MockitoAnnotations.initMocks(this);
        appointmentsService = new AppointmentsService(
            bookingRepository,
            new AuthenticationFacade(),
            locationService,
            referenceDomainService,
            telemetryClient,
            scheduledAppointmentRepository);
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Creating appointments")
    class CreateAppointments {
        @Test
        public void createAppointments_returnsMultipleAppointmentDetails() {
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

            final var appointment1 = appointmentsToCreate.withDefaults().get(0);
            final var createdId1 = 1L;
            final var appointment2 = appointmentsToCreate.withDefaults().get(1);
            final var createdId2 = 2L;

            when(bookingRepository.createAppointment(
                appointment1,
                appointmentsToCreate.getAppointmentDefaults(),
                LOCATION_B.getAgencyId()
            )).thenReturn(createdId1);

            when(bookingRepository.createAppointment(
                appointment2,
                appointmentsToCreate.getAppointmentDefaults(),
                LOCATION_B.getAgencyId()
            )).thenReturn(createdId2);

            final var createdAppointmentDetails = appointmentsService.createAppointments(appointmentsToCreate);

            assertThat(createdAppointmentDetails).hasSize(2)
                .containsExactlyInAnyOrder(
                    CreatedAppointmentDetails.builder()
                        .appointmentEventId(createdId1)
                        .bookingId(appointment1.getBookingId())
                        .startTime(appointment1.getStartTime())
                        .endTime(appointment1.getEndTime())
                        .locationId(LOCATION_B.getLocationId())
                        .appointmentType(REFERENCE_CODE_T.getCode())
                        .build(),
                    CreatedAppointmentDetails.builder()
                        .appointmentEventId(createdId2)
                        .bookingId(appointment2.getBookingId())
                        .startTime(appointment2.getStartTime())
                        .endTime(appointment2.getEndTime())
                        .locationId(LOCATION_B.getLocationId())
                        .appointmentType(REFERENCE_CODE_T.getCode())
                        .build()
                );

            verify(telemetryClient).trackEvent("AppointmentsCreated",
                Map.of(
                    "defaultStart", appointmentsToCreate.getAppointmentDefaults().getStartTime().toString(),
                    "count", "2",
                    "type", "T",
                    "location", "1",
                    "user", "username"
                ),
                null);
        }

        @Test
        public void createAppointments_returnsRepeatAppointmentIds() {
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
                .repeat(Repeat.builder()
                    .count(3)
                    .repeatPeriod(RepeatPeriod.DAILY)
                    .build())
                .build();


            final var createdId1 = 1L;
            final var recurringId1 = 2L;
            final var recurringId2 = 3L;

            final var appointmentWithRepeats = AppointmentsService
                .withRepeats(appointmentsToCreate.getRepeat(), appointmentsToCreate.withDefaults().get(0));

            when(bookingRepository.createAppointment(
                appointmentWithRepeats.get(0),
                appointmentsToCreate.getAppointmentDefaults(),
                LOCATION_B.getAgencyId()
            )).thenReturn(createdId1);

            when(bookingRepository.createAppointment(
                appointmentWithRepeats.get(1),
                appointmentsToCreate.getAppointmentDefaults(),
                LOCATION_B.getAgencyId()
            )).thenReturn(recurringId1);

            when(bookingRepository.createAppointment(
                appointmentWithRepeats.get(2),
                appointmentsToCreate.getAppointmentDefaults(),
                LOCATION_B.getAgencyId()
            )).thenReturn(recurringId2);

            final var createdAppointmentDetails = appointmentsService.createAppointments(appointmentsToCreate);

            assertThat(createdAppointmentDetails).hasSize(3)
                .containsExactlyInAnyOrder(
                    CreatedAppointmentDetails.builder()
                        .appointmentEventId(createdId1)
                        .bookingId(appointmentWithRepeats.get(0).getBookingId())
                        .startTime(appointmentWithRepeats.get(0).getStartTime())
                        .endTime(appointmentWithRepeats.get(0).getEndTime())
                        .locationId(LOCATION_B.getLocationId())
                        .appointmentType(REFERENCE_CODE_T.getCode())
                        .build(),
                    CreatedAppointmentDetails.builder()
                        .appointmentEventId(recurringId1)
                        .bookingId(appointmentWithRepeats.get(1).getBookingId())
                        .startTime(appointmentWithRepeats.get(1).getStartTime())
                        .endTime(appointmentWithRepeats.get(1).getEndTime())
                        .locationId(LOCATION_B.getLocationId())
                        .appointmentType(REFERENCE_CODE_T.getCode())
                        .build(),
                    CreatedAppointmentDetails.builder()
                        .appointmentEventId(recurringId2)
                        .bookingId(appointmentWithRepeats.get(2).getBookingId())
                        .startTime(appointmentWithRepeats.get(2).getStartTime())
                        .endTime(appointmentWithRepeats.get(2).getEndTime())
                        .locationId(LOCATION_B.getLocationId())
                        .appointmentType(REFERENCE_CODE_T.getCode())
                        .build()
                );

            verify(telemetryClient).trackEvent("AppointmentsCreated",
                Map.of(
                    "defaultStart", appointmentsToCreate.getAppointmentDefaults().getStartTime().toString(),
                    "count", "3",
                    "type", "T",
                    "location", "1",
                    "user", "username"
                ),
                null);
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Request to create 1001 appointments exceeds limit of 1000");

            verifyNoMoreInteractions(telemetryClient);
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Location does not exist or is not in your caseload.");

            verifyNoMoreInteractions(telemetryClient);
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Event type not recognised.");

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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("A BookingId does not exist in your caseload");

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
                .createAppointment(
                    appointmentsToCreate.withDefaults().get(0),
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("An appointment startTime is later than the limit of ");
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("An appointment endTime is later than the limit of ");
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Appointment end time is before the start time.");
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
                .createAppointment(
                    appointmentsToCreate.withDefaults().get(0),
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
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.");
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
                .createAppointment(
                    appointmentsToCreate.withDefaults().get(0),
                    appointmentsToCreate.getAppointmentDefaults(),
                    LOCATION_B.getAgencyId());
        }

    }


    @Nested
    @DisplayName("Repeatable appointments")
    class Repeats {
        @Test
        public void shouldHandleNoRepeats() {
            assertThat(AppointmentsService.withRepeats(null, Collections.singletonList(DETAILS_2)))
                .containsExactly(DETAILS_2);
        }

        @Test
        public void shouldHandleOneRepeat() {
            assertThat(AppointmentsService.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(1).build(),
                List.of(DETAILS_2)
            ))
                .containsExactly(DETAILS_2);
        }

        @Test
        public void shouldHandleMultipleRepeats() {
            assertThat(AppointmentsService.withRepeats(
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
        public void shouldRepeatMultipleAppointments() {
            assertThat(AppointmentsService.withRepeats(
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
        public void shouldHandleNullEndTime() {
            assertThat(AppointmentsService.withRepeats(
                Repeat.builder().repeatPeriod(RepeatPeriod.DAILY).count(2).build(),
                List.of(DETAILS_3)
            ))
                .containsExactly(
                    DETAILS_3,
                    DETAILS_3.toBuilder().startTime(DETAILS_3.getStartTime().plusDays(1)).build()
                );
        }

    }


    @Nested
    @DisplayName("Creating a single appointment for a booking")
    class CreatedBookingAppointments {
        @Test
        public void testCreateBookingAppointment() {

            final var appointmentType = "MEDE";
            final var locationId = -20L;
            final var bookingId = 100L;
            final var agencyId = "LEI";
            final var eventId = -10L;
            final var principal = "ME";
            final var createdEventId = 999L;
            final var expectedEvent = ScheduledEvent
                .builder()
                .bookingId(bookingId)
                .eventId(createdEventId)
                .eventLocationId(locationId)
                .build();
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

            when(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent));
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
            } catch (final HttpClientErrorException e) {
                assertThat(e.getStatusText()).isEqualTo("Appointment time is in the past.");
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
            } catch (final HttpClientErrorException e) {
                assertThat(e.getStatusText()).isEqualTo("Appointment end time is before the start time.");
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

            when(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent));

            when(locationService.getLocation(newAppointment.getLocationId()))
                .thenThrow(new EntityNotFoundException("test"));

            assertThatThrownBy(() -> appointmentsService.createBookingAppointment(bookingId, principal, newAppointment))
                .isInstanceOf(HttpClientErrorException.class).hasMessageContaining("Location does not exist or is not in your caseload.");
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

            when(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent));

            when(referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(), false))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentsService.createBookingAppointment(bookingId, principal, newAppointment))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("Event type not recognised.");
        }

        @Test
        public void testOverrideAgencyLocationTest() {
            final var appointmentType = "MEDE";
            final var locationId = -20L;
            final var bookingId = 100L;
            final var agencyId = "LEI";
            final var eventId = -10L;
            final var principal = "ME";
            final var expectedEvent = ScheduledEvent
                .builder()
                .bookingId(bookingId)
                .eventId(eventId)
                .eventLocationId(locationId)
                .build();

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

            when(bookingRepository.getBookingAppointmentByEventId(eventId)).thenReturn(Optional.of(expectedEvent));

            appointmentsService.createBookingAppointment(bookingId, principal, newAppointment);

            verify(locationService, never()).getUserLocations(principal);
        }
    }

    @Nested
    @DisplayName("Get appointments")
    class GetAppointment {
        @Test
        public void testFindByAgencyIdAndEventDateAndLocationId_IsCalledCorrectly() {
            final var today = LocalDate.now();
            final var locationId = 1L;

            when(scheduledAppointmentRepository.findByAgencyIdAndEventDateAndLocationId(any(), any(), anyLong())).thenReturn(Collections.emptyList());

            appointmentsService.getAppointments("LEI", today, locationId, null);

            verify(scheduledAppointmentRepository).findByAgencyIdAndEventDateAndLocationId("LEI", today, locationId);
        }

        @Test
        public void testFindByAgencyIdAndEventDate_IsCalledCorrectly_IsCalledCorrectly() {
            final var today = LocalDate.now();

            when(scheduledAppointmentRepository.findByAgencyIdAndEventDate(any(), any()))
                .thenReturn(Collections.emptyList());

            appointmentsService.getAppointments("LEI", today, null, null);

            verify(scheduledAppointmentRepository).findByAgencyIdAndEventDate("LEI", today);

        }

        @Test
        public void testAMScheduledAppointmentDtos_AreReturned() {
            final var today = LocalDate.now();
            final var startTime = LocalDateTime.now();
            final var endTime = LocalDateTime.now();

            when(scheduledAppointmentRepository.findByAgencyIdAndEventDate(any(), any()))
                .thenReturn(List.of(
                    ScheduledAppointment
                        .builder()
                        .eventId(1L)
                        .offenderNo("A12345")
                        .firstName("firstName1")
                        .lastName("lastName1")
                        .eventDate(today)
                        .startTime(startTime.withHour(11))
                        .endTime(endTime.withHour(11))
                        .appointmentTypeDescription("Appointment Type Description1")
                        .appointmentTypeCode("appointmentTypeCode1")
                        .locationDescription("location Description1")
                        .locationId(1L)
                        .createUserId("Staff user 1")
                        .agencyId("LEI")
                        .build(),
                    ScheduledAppointment
                        .builder()
                        .eventId(2L)
                        .offenderNo("A12346")
                        .firstName("firstName2")
                        .lastName("lastName2")
                        .eventDate(today)
                        .startTime(startTime.withHour(23))
                        .endTime(endTime.withHour(23))
                        .appointmentTypeDescription("appointmentTypeDescription2")
                        .appointmentTypeCode("appointmentTypeCode2")
                        .locationDescription("location Description2")
                        .locationId(2L)
                        .createUserId("Staff user 2")
                        .agencyId("LEI")
                        .build()
                ));

            final var appointmentDtos = appointmentsService.getAppointments("LEI", today, null, TimeSlot.AM);

            assertThat(appointmentDtos).containsOnly(
                ScheduledAppointmentDto
                    .builder()
                    .id(1L)
                    .offenderNo("A12345")
                    .firstName("firstName1")
                    .lastName("lastName1")
                    .date(today)
                    .startTime(startTime.withHour(11))
                    .endTime(endTime.withHour(11))
                    .appointmentTypeDescription("Appointment Type Description1")
                    .appointmentTypeCode("appointmentTypeCode1")
                    .locationDescription("Location Description1")
                    .locationId(1L)
                    .createUserId("Staff user 1")
                    .agencyId("LEI")
                    .build());
        }

        @Test
        public void testScheduledAppointmentsOrderedByStartTimeThenByLocation() {
            final var baseDateTime = LocalDateTime.now().minusDays(1);

            when(scheduledAppointmentRepository.findByAgencyIdAndEventDate(any(), any()))
                .thenReturn(List.of(
                    ScheduledAppointment.builder().eventId(1L).startTime(baseDateTime.withHour(23)).locationDescription("Gym").build(),
                    ScheduledAppointment.builder().eventId(2L).startTime(baseDateTime.withHour(11)).locationDescription("Room 2").build(),
                    ScheduledAppointment.builder().eventId(3L).startTime(baseDateTime.withHour(10)).locationDescription("Z").build(),
                    ScheduledAppointment.builder().eventId(4L).startTime(baseDateTime.withHour(10)).locationDescription("A").build()
                ));

            final var appointmentDtos = appointmentsService.getAppointments("LEI", LocalDate.now(), null, null);

            assertThat(appointmentDtos)
                .extracting(
                    ScheduledAppointmentDto::getId,
                    ScheduledAppointmentDto::getLocationDescription
                ).containsExactly(Tuple.tuple(4L, "A"), Tuple.tuple(3L, "Z"), Tuple.tuple(2L, "Room 2"), Tuple.tuple(1L, "Gym"));
        }
    }

    @Nested
    @DisplayName("Delete a single appointment")
    class DeleteSingleAppointment {
        @Test
        public void deleteBookingAppointment_notFound() {
            when(bookingRepository.getBookingAppointmentByEventId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentsService.deleteBookingAppointment(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking Appointment for eventId 1 not found.");
        }

        @Test
        public void deleteBookingAppointment() {
            final var scheduledEvent = ScheduledEvent
                .builder()
                .eventId(1L)
                .eventType("APP")
                .eventSubType("VLB")
                .startTime(LocalDateTime.of(2020, 1, 1, 1, 1))
                .endTime(LocalDateTime.of(2020, 1, 1, 1, 31))
                .eventLocationId(2L)
                .agencyId("WWI")
                .build();
            when(bookingRepository.getBookingAppointmentByEventId(1L)).thenReturn(Optional.of(scheduledEvent));

            appointmentsService.deleteBookingAppointment(1L);

            verify(bookingRepository).deleteBookingAppointment(1L);
            verify(telemetryClient).trackEvent(
                "AppointmentDeleted",
                Map.of(
                    "eventId", "1",
                    "type", "VLB",
                    "start", "2020-01-01T01:01",
                    "end", "2020-01-01T01:31",
                    "location", "2",
                    "agency", "WWI",
                    "user", "username"
                ),
                null);
        }
    }


    @Nested
    @DisplayName("Delete multiple appointments")
    class DeleteMultipleAppointments {
        @Test
        public void attemptToDeleteAppointmentsThatExist() {
            when(bookingRepository.getBookingAppointmentByEventId(1L))
                .thenReturn(Optional.of(ScheduledEvent.builder()
                    .eventId(1L)
                    .eventSubType("APP")
                    .startTime(LocalDateTime.parse("2020-01-01T01:01"))
                    .endTime(LocalDateTime.parse("2020-01-01T01:31"))
                    .eventLocationId(2L)
                    .agencyId("LEI")
                    .createUserId("username")
                    .build()));

            when(bookingRepository.getBookingAppointmentByEventId(2L))
                .thenReturn(Optional.empty());

            appointmentsService.deleteBookingAppointments(List.of(1L, 2L));

            verify(bookingRepository, times(1)).deleteBookingAppointment(anyLong());
            verify(bookingRepository).deleteBookingAppointment(1L);

            verify(telemetryClient).trackEvent(
                "AppointmentDeleted",
                Map.of(
                    "eventId", "1",
                    "type", "APP",
                    "start", "2020-01-01T01:01",
                    "end", "2020-01-01T01:31",
                    "location", "2",
                    "agency", "LEI",
                    "user", "username"
                ),
                null);
        }
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

    private void ensureRoles(final String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(USERNAME, null, roles));
    }

}

