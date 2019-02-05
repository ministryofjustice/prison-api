package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.bulkappointments.*;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AppointmentsService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes.failure;
import static net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes.success;

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
    public CreateAppointmentsOutcomes createAppointments(@NotNull @Valid NewAppointments appointments) {

        final UnaryOperator<AppointmentDetails> withDefaults = defaultsDecoratorFactory(appointments.getAppointmentDefaults());

        final Stream<CreateAppointmentsOutcomes> outcomes = appointments
                .getAppointments()
                .stream()
                .map(withDefaults)
                .map(this::createAppointment);

        return mergeCreateAppointmentOutcomes(outcomes);
    }

    static UnaryOperator<AppointmentDetails> defaultsDecoratorFactory(AppointmentDefaults defaults) {
        return appointment -> {
            var b = appointment.toBuilder();
            if (null == appointment.getAppointmentType()) b.appointmentType(defaults.getAppointmentType());
            if (null == appointment.getStartTime()) b.startTime(defaults.getStartTime());
            if (null == appointment.getEndTime()) b.endTime(defaults.getEndTime());
            if (null == appointment.getLocationId()) b.locationId(defaults.getLocationId());
            if (null == appointment.getComment()) b.comment(defaults.getComment());
            return b.build();
        };
    }

    CreateAppointmentsOutcomes createAppointment(AppointmentDetails appointment) {
        try {
            return success(
                    bookingService.createBookingAppointment(
                            bookingService.getBookingIdByOffenderNo(appointment.getOffenderNo()),
                            authenticationFacade.getCurrentUsername(),
                            toNewAppointment(appointment)));
        } catch (Exception e) {
            return failure(RejectedAppointment
                    .builder()
                    .errorResponse(ResourceExceptionHandler.processResponse(e))
                    .appointmentDetails(appointment)
                    .build());
        }
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

    static CreateAppointmentsOutcomes mergeCreateAppointmentOutcomes(Stream<CreateAppointmentsOutcomes> outcomes) {
        return outcomes.collect(
                CreateAppointmentsOutcomes::accumulator,
                CreateAppointmentsOutcomes::add,
                CreateAppointmentsOutcomes::add
        );
    }
}