package net.syscon.elite.service.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.*;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AppointmentsService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Service
@Validated
public class AppointmentsServiceImpl implements AppointmentsService {

    private final BookingService bookingService;
    private final AuthenticationFacade authenticationFacade;

    public AppointmentsServiceImpl(
            BookingService bookingService,
            AuthenticationFacade authenticationFacade) {
        this.bookingService = bookingService;
        this.authenticationFacade = authenticationFacade;
    }

    /**
     * Create multiple appointments (ScheduledEvents?).
     * This implementation creates each appointment using BookingService#createBookingAppointment.
     *
     * @param appointments Details of the new appointments to be created.
     * @return The outcomes of creating each of the desired appointments.
     */
    @Override
    public CreateAppointmentsOutcomes createAppointments(@Valid NewAppointments appointments) {

        final UnaryOperator<AppointmentDetails> withDefaults = defaultsDecoratorFactory(appointments.getAppointmentDefaults());

        final Stream<Either<RejectedAppointment, ScheduledEvent>> outcomes = appointments
                .getAppointments()
                .stream()
                .map(withDefaults)
                .map(this::createAppointment);

        return toCreateAppointmentOutcomes(outcomes);
    }

    static UnaryOperator<AppointmentDetails> defaultsDecoratorFactory(AppointmentDefaults defaults) {
        return appointment -> {
            AppointmentDetails.AppointmentDetailsBuilder b = appointment.toBuilder();
            if (appointment.getAppointmentType() == null) b.appointmentType(defaults.getAppointmentType());
            if (appointment.getStartTime() == null) b.startTime(defaults.getStartTime());
            if (appointment.getEndTime() == null) b.endTime(defaults.getEndTime());
            if (appointment.getLocationId() == null) b.locationId(defaults.getLocationId());
            if (appointment.getComment() == null) b.comment(defaults.getComment());
            return b.build();
        };
    }


    Either<RejectedAppointment, ScheduledEvent> createAppointment(AppointmentDetails appointment) {
        return Try.of(() ->
                bookingService.createBookingAppointment(
                        bookingService.getBookingIdByOffenderNo(appointment.getOffenderNo()),
                        authenticationFacade.getCurrentUsername(),
                        toNewAppointment(appointment)))
                .toEither()
                .mapLeft(AppointmentsServiceImpl::throwableToErrorResponse)
                .mapLeft(errorResponse -> RejectedAppointment
                        .builder()
                        .errorResponse(errorResponse)
                        .appointmentDetails(appointment)
                        .build());
    }

    static NewAppointment toNewAppointment(AppointmentDetails appointment) {
        return NewAppointment
                .builder()
                .appointmentType(appointment.getAppointmentType())
                .comment(appointment.getComment())
                .endTime(appointment.getEndTime())
                .startTime(appointment.getStartTime())
                .locationId(appointment.getLocationId())
                .build();
    }

    static CreateAppointmentsOutcomes toCreateAppointmentOutcomes(Stream<Either<RejectedAppointment, ScheduledEvent>> outcomes) {
        return outcomes.collect(
                CreateAppointmentsOutcomes::new,
                (acc, either) -> {
                    if (either.isRight()) {
                        acc.getCreatedEvents().add(either.get());
                    } else {
                        acc.getRejectedAppointments().add(either.getLeft());
                    }
                },
                (l, r) -> {
                    l.getCreatedEvents().addAll(r.getCreatedEvents());
                    l.getRejectedAppointments().addAll((r.getRejectedAppointments()));
                }
        );
    }

    static ErrorResponse throwableToErrorResponse(Throwable t) {
        if (t instanceof Exception) return ResourceExceptionHandler.processResponse((Exception) t);

        // Generic response for anything else...
        return ErrorResponse.builder()
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .userMessage("An internal error has occurred - please try again later.")
                .developerMessage(t.getMessage())
                .build();
    }
}