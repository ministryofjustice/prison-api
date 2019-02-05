package net.syscon.elite.service.impl;

import io.vavr.control.Either;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.*;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AppointmentsServiceImplTest {
    private static final String USERNAME = "username";
    private static final String OFFENDER_NO = "A1234AX";
    private static final Long BOOKING_ID = 100L;
    private static final Long LOCATION_ID = 9090L;
    private static final String APPOINTMENT_TYPE = "AT";
    private static final LocalDateTime START_TIME = LocalDateTime.of(2017, 1, 1, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2021, 1, 1, 0, 0);
    private static final String COMMENT = "C";

    private static final String DEFAULT_APPOINTMENT_TYPE = "DAT";
    private static final String DEFAULT_COMMENT = "DC";
    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2019, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2019, 1, 1, 1, 0);
    private static final Long DEFAULT_LOCATION_ID = 10101L;

    @Mock
    private BookingService bookingService;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private AppointmentsServiceImpl appointmentsService;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(authenticationFacade.getCurrentUsername()).thenReturn(USERNAME);
    }

    @Test
    public void shouldApplyDefaults() {
        var decorator = AppointmentsServiceImpl.defaultsDecoratorFactory(fullDefaults());

        final var actual = decorator.apply(AppointmentDetails.builder().OffenderNo(OFFENDER_NO).build());
        final var expected = AppointmentDetails
                .builder()
                .OffenderNo(OFFENDER_NO)
                .appointmentType(DEFAULT_APPOINTMENT_TYPE)
                .locationId(DEFAULT_LOCATION_ID)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .comment(DEFAULT_COMMENT)
                .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void actualShouldOverrideDefaults() {
        var decorator = AppointmentsServiceImpl.defaultsDecoratorFactory(fullDefaults());

        final var override = AppointmentDetails
                .builder()
                .OffenderNo(OFFENDER_NO)
                .appointmentType(APPOINTMENT_TYPE)
                .locationId(LOCATION_ID)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .comment(COMMENT)
                .build();

        final var actual = decorator.apply(override);

        assertThat(actual).isEqualTo(override);
    }

    private static AppointmentDefaults fullDefaults() {
        return AppointmentDefaults
                .builder()
                .appointmentType(DEFAULT_APPOINTMENT_TYPE)
                .locationId(DEFAULT_LOCATION_ID)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .comment(DEFAULT_COMMENT)
                .build();
    }

    @Test
    public void toNewAppointment() {
        assertThat(AppointmentsServiceImpl.toNewAppointment(
                AppointmentDetails
                        .builder()
                        .OffenderNo(OFFENDER_NO)
                        .appointmentType(APPOINTMENT_TYPE)
                        .locationId(LOCATION_ID)
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .comment(COMMENT)
                        .build()))
                .isEqualTo(
                        NewAppointment.builder()
                                .appointmentType(APPOINTMENT_TYPE)
                                .locationId(LOCATION_ID)
                                .startTime(START_TIME)
                                .endTime(END_TIME)
                                .comment(COMMENT)
                                .build()
                );
    }

    @Test
    public void shouldConvertThrowableToErrorResponse() {
        assertThat(AppointmentsServiceImpl.throwableToErrorResponse(new Throwable("my message")))
                .isEqualTo(ErrorResponse.builder()
                        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .userMessage("An internal error has occurred - please try again later.")
                        .developerMessage("my message")
                        .build());
    }

    @Test
    public void toCreateAppointmentOutcomesNoOutcomes() {
        var outcomes = AppointmentsServiceImpl.toCreateAppointmentOutcomes(Stream.empty());
        assertThat(outcomes.getCreatedEvents()).isEmpty();
        assertThat(outcomes.getRejectedAppointments()).isEmpty();
    }

    @Test
    public void toCreateAppointmentOutcomesOneSuccess() {
        var scheduledEvent = ScheduledEvent.builder().eventId(1L).build();
        var outcomes = AppointmentsServiceImpl.toCreateAppointmentOutcomes(Stream.of(Either.right(scheduledEvent)));
        assertThat(outcomes.getCreatedEvents()).containsExactly(scheduledEvent);
        assertThat(outcomes.getRejectedAppointments()).isEmpty();
    }

    @Test
    public void toCreateAppointmentOutcomesOneFailure() {
        var rejectedAppointment = RejectedAppointment.builder().build();

        var outcomes = AppointmentsServiceImpl.toCreateAppointmentOutcomes(Stream.of(Either.left(rejectedAppointment)));
        assertThat(outcomes.getCreatedEvents()).isEmpty();
        assertThat(outcomes.getRejectedAppointments()).containsExactly(rejectedAppointment);
    }

    @Test
    public void toCreateAppointmentOutcomesSuccessesAndFailures() {
        var ra1 = RejectedAppointment.builder().build();
        var ra2 = RejectedAppointment.builder().build();
        var se1 = ScheduledEvent.builder().eventId(1L).build();
        var se2 = ScheduledEvent.builder().eventId(2L).build();


        var outcomes = AppointmentsServiceImpl.toCreateAppointmentOutcomes(Stream.of(
                Either.left(ra1),
                Either.right(se1),
                Either.right(se2),
                Either.left(ra2)
        ));
        assertThat(outcomes.getCreatedEvents()).containsExactly(se1, se2);
        assertThat(outcomes.getRejectedAppointments()).containsExactly(ra1, ra2);
    }


    @Test
    public void shouldCreateAppointment() {
        var details = AppointmentDetails.builder().OffenderNo(OFFENDER_NO).locationId(LOCATION_ID).build();
        var se = ScheduledEvent.builder().build();

        when(bookingService.getBookingIdByOffenderNo(any())).thenReturn(BOOKING_ID);
        when(authenticationFacade.getCurrentUsername()).thenReturn(USERNAME);
        when(bookingService.createBookingAppointment(any(), any(), any(NewAppointment.class))).thenReturn(se);

        var result = appointmentsService.createAppointment(details);

        verify(bookingService).getBookingIdByOffenderNo(OFFENDER_NO);
        verify(bookingService).createBookingAppointment(BOOKING_ID, USERNAME, NewAppointment.builder().locationId(LOCATION_ID).build());

        assertThat(result).isEqualTo(Either.right(se));
    }

    @Test
    public void shouldReportOffenderNoToIdFailure() {
        var details = AppointmentDetails.builder().OffenderNo(OFFENDER_NO).locationId(LOCATION_ID).build();

        when(bookingService.getBookingIdByOffenderNo(any())).thenThrow(EntityNotFoundException.withId(OFFENDER_NO));
        when(authenticationFacade.getCurrentUsername()).thenReturn(USERNAME);

        var result = appointmentsService.createAppointment(details);

        verify(bookingService).getBookingIdByOffenderNo(OFFENDER_NO);

        assertThat(result).isEqualTo(Either.left(
                RejectedAppointment.builder()
                        .appointmentDetails(AppointmentDetails
                                .builder()
                                .OffenderNo(OFFENDER_NO)
                                .locationId(LOCATION_ID)
                                .build())
                        .errorResponse(ErrorResponse
                                .builder()
                                .status(404)
                                .userMessage("Resource with id [A1234AX] not found.")
                                .developerMessage("")
                                .build())
                        .build()));
    }

    @Test
    public void shouldCreateNoAppointments() {
        var newAppointments = NewAppointments
                .builder()
                .appointmentDefaults(AppointmentDefaults.builder().build())
                .appointments(Collections.emptyList())
                .build();

        CreateAppointmentsOutcomes outcomes = appointmentsService.createAppointments(newAppointments);

        assertThat(outcomes.getCreatedEvents()).isEmpty();
        assertThat(outcomes.getRejectedAppointments()).isEmpty();
    }

    @Test
    public void shouldCreateSingleAppointment() {

        final var newAppointments = NewAppointments
                .builder()
                .appointmentDefaults(AppointmentDefaults.builder().build())
                .appointments(Collections.singletonList(AppointmentDetails
                        .builder()
                        .OffenderNo(OFFENDER_NO)
                        .locationId(1L)
                        .startTime(LocalDateTime.of(2019, 1, 1, 9, 0))
                        .build()))
                .build();

        when(bookingService.getBookingIdByOffenderNo(OFFENDER_NO)).thenReturn(BOOKING_ID);

        final ScheduledEvent event = ScheduledEvent.builder().eventId(1000L).build();

        when(bookingService.createBookingAppointment(eq(BOOKING_ID), eq(USERNAME), any())).thenReturn(event);

        CreateAppointmentsOutcomes outcomes = appointmentsService.createAppointments(newAppointments);

        assertThat(outcomes.getCreatedEvents()).containsExactly(event);
        assertThat(outcomes.getRejectedAppointments()).isEmpty();
    }

    @Test
    public void shouldHandleMultipleAppointments() {

        final var newAppointments = NewAppointments
                .builder()
                .appointmentDefaults(fullDefaults())
                .appointments(Arrays.asList(
                        AppointmentDetails
                                .builder()
                                .OffenderNo(OFFENDER_NO)
                                .locationId(LOCATION_ID)
                                .appointmentType(APPOINTMENT_TYPE)
                                .startTime(START_TIME)
                                .endTime(END_TIME)
                                .comment(COMMENT)
                                .build(),
                        AppointmentDetails
                                .builder()
                                .OffenderNo("ON2")
                                .build(),
                        AppointmentDetails
                                .builder()
                                .OffenderNo("ON3")
                                .build()))
                .build();

        when(bookingService.getBookingIdByOffenderNo(OFFENDER_NO)).thenReturn(BOOKING_ID);
        when(bookingService.getBookingIdByOffenderNo("ON2")).thenThrow(new NullPointerException());
        when(bookingService.getBookingIdByOffenderNo("ON3")).thenReturn(BOOKING_ID + 1);

        final ScheduledEvent e1 = ScheduledEvent.builder().eventId(1000L).build();
        final ScheduledEvent e2 = ScheduledEvent.builder().eventId(10001L).build();

        when(bookingService.createBookingAppointment(eq(BOOKING_ID), eq(USERNAME), any())).thenReturn(e1);
        when(bookingService.createBookingAppointment(eq(BOOKING_ID+1), eq(USERNAME), any())).thenReturn(e2);

        CreateAppointmentsOutcomes outcomes = appointmentsService.createAppointments(newAppointments);

        assertThat(outcomes.getCreatedEvents()).containsExactly(e1, e2);
        assertThat(outcomes.getRejectedAppointments()).hasSize(1);
    }
}
