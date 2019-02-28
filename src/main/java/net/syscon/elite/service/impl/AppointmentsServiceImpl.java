package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.elite.api.model.bulkappointments.Repeat;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AppointmentsService;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.syscon.elite.security.AuthenticationFacade.hasRoles;

@Service
@Validated
@Transactional
public class AppointmentsServiceImpl implements AppointmentsService {

    // Maximum of 1000 values in an Oracle 'IN' clause is current hard limit. (See #validateBookingIds below).
    private static final int MAXIMUM_NUMBER_OF_APPOINTMENTS = 1000;
    private static final int APPOINTMENT_TIME_LIMIT_IN_DAYS = 365;

    private final BookingRepository bookingRepository;
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final ReferenceDomainService referenceDomainService;
    private final TelemetryClient telemetryClient;

    public AppointmentsServiceImpl(
            BookingRepository bookingRepository,
            AuthenticationFacade authenticationFacade,
            LocationService locationService,
            ReferenceDomainService referenceDomainService,
            TelemetryClient telemetryClient) {
        this.bookingRepository = bookingRepository;
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.referenceDomainService = referenceDomainService;
        this.telemetryClient = telemetryClient;
    }

    /**
     * Create multiple appointments (ScheduledEvents?).
     * This implementation creates each appointment using BookingService#createBookingAppointment.
     *
     * @param appointments Details of the new appointments to be created.
     */
    @PreAuthorize("#oauth2.hasScope('write')")

    @Override
    public void createAppointments(@NotNull @Valid AppointmentsToCreate appointments) {

        assertThatRequestHasPermission(appointments);
        assertFewerThanMaximumNumberOfBookingIds(appointments);

        final var defaults = appointments.getAppointmentDefaults();

        final String agencyId = findLocationInUserLocations(defaults.getLocationId())
                .orElseThrow(() -> new BadRequestException("Location does not exist or is not in your caseload."))
                .getAgencyId();

        assertValidAppointmentType(defaults.getAppointmentType());
        assertAllBookingIdsInCaseload(appointments.getAppointments(), agencyId);

        final var flattenedDetails = appointments.withDefaults();

        assertAdditionalAppointmentConstraints(flattenedDetails);

        final var withRepeats = withRepeats(appointments.getRepeat(), flattenedDetails);

        assertThatAppointmentsFallWithin(withRepeats, appointmentTimeLimit());
        createAppointments(withRepeats, defaults, agencyId);
    }

    private void assertThatRequestHasPermission(AppointmentsToCreate appointments) {
        if (appointments.moreThanOneOffender() && !hasRoles("BULK_APPOINTMENTS")) {
            throw new BadRequestException("You do not have the 'BULK_APPOINTMENTS' role. Creating appointments for more than one offender is not permitted without this role.");
        }
    }

    private void assertThatAppointmentsFallWithin(List<AppointmentDetails> appointments, LocalDateTime limit) {
        for (var appointment : appointments) {
            assertThatAppointmentFallsWithin(appointment, limit);
        }
    }

    private void assertThatAppointmentFallsWithin(AppointmentDetails appointment, LocalDateTime limit) {
        if (appointment.getStartTime().isAfter(limit)) {
            throw new BadRequestException("An appointment startTime is later than the limit of " + limit);
        }
        if (appointment.getEndTime() == null) return;
        if (appointment.getEndTime().isAfter(limit)) {
            throw new BadRequestException("An appointment endTime is later than the limit of " + limit);
        }
    }

    private static LocalDateTime appointmentTimeLimit() {
        return LocalDateTime.now().plusDays(APPOINTMENT_TIME_LIMIT_IN_DAYS);
    }

    private void assertFewerThanMaximumNumberOfBookingIds(AppointmentsToCreate appointments) {
        final int numberOfAppointments = appointments.getAppointments().size();

        if (numberOfAppointments > MAXIMUM_NUMBER_OF_APPOINTMENTS) {
            throw new BadRequestException("Request to create " + numberOfAppointments + " appointments exceeds limit of " + MAXIMUM_NUMBER_OF_APPOINTMENTS);
        }
    }

    private void assertAllBookingIdsInCaseload(List<AppointmentDetails> appointments, String agencyId) {
        List<Long> bookingIds = appointments.stream().map(AppointmentDetails::getBookingId).collect(Collectors.toList());
        List<Long> bookingIdsInAgency = bookingRepository.findBookingsIdsInAgency(bookingIds, agencyId);
        if (bookingIdsInAgency.size() < bookingIds.size()) {
            throw new BadRequestException("A BookingId does not exist in your caseload");
        }
    }

    private void assertAdditionalAppointmentConstraints(List<AppointmentDetails> appointments) {
        appointments.forEach(AppointmentsServiceImpl::assertStartTimePrecedesEndTime);
    }

    private static void assertStartTimePrecedesEndTime(AppointmentDetails appointment) {
        if (appointment.getEndTime() != null
                && appointment.getEndTime().isBefore(appointment.getStartTime())) {
            throw new BadRequestException("Appointment end time is before the start time.");
        }
    }

    private void assertValidAppointmentType(String appointmentType) {
        findEventType(appointmentType).orElseThrow(() -> new BadRequestException("Event type not recognised."));
    }

    private Optional<ReferenceCode> findEventType(String appointmentType) {
        return referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(),
                appointmentType,
                false);
    }

    private Optional<Location> findLocationInUserLocations(long locationId) {

        Location appointmentLocation = locationService.getLocation(locationId);
        List<Location> userLocations = locationService.getUserLocations(authenticationFacade.getCurrentUsername());

        for (Location location : userLocations) {
            if (location.getAgencyId().equals(appointmentLocation.getAgencyId())) {
                return Optional.of(location);
            }
        }
        return Optional.empty();
    }

    private void trackAppointmentsCreated(List<AppointmentDetails> appointments, AppointmentDefaults defaults) {
        if (appointments.size() < 1) return;

        final Map<String, String> logMap = new HashMap<>();
        logMap.put("type", defaults.getAppointmentType());
        logMap.put("defaultStart", defaults.getStartTime().toString());
        logMap.put("location", defaults.getLocationId().toString());
        logMap.put("user", authenticationFacade.getCurrentUsername());
        if (defaults.getEndTime() != null) {
            logMap.put("defaultEnd", defaults.getEndTime().toString());
        }
        logMap.put("count", Integer.toString(appointments.size()));

        telemetryClient.trackEvent("AppointmentsCreated", logMap, null);
    }

    static List<AppointmentDetails> withRepeats(Repeat repeat, List<AppointmentDetails> details) {
        if (repeat == null) return details;
        return details.stream()
                .flatMap(d -> withRepeats(repeat, d))
                .collect(Collectors.toList());
    }

    static Stream<AppointmentDetails> withRepeats(Repeat repeat, AppointmentDetails details) {
        final var appointmentDuration = Optional
                .ofNullable(details.getEndTime())
                .map(endTime -> Duration.between(details.getStartTime(), endTime));

        return repeat
                .dateTimeStream(details.getStartTime())
                .map(startTime -> buildFromPrototypeWithStartTimeAndDuration(details, startTime, appointmentDuration));
    }


    private static AppointmentDetails buildFromPrototypeWithStartTimeAndDuration(AppointmentDetails prototype,
                                                                                 LocalDateTime startTime,
                                                                                 Optional<Duration> appointmentDuration) {
        var builder = prototype.toBuilder().startTime(startTime);
        appointmentDuration.ifPresent(d -> builder.endTime(startTime.plus(d)));
        return builder.build();
    }


    private void createAppointments(List<AppointmentDetails> details, AppointmentDefaults defaults, String agencyId) {
        bookingRepository.createMultipleAppointments(details, defaults, agencyId);
        trackAppointmentsCreated(details, defaults);
    }
}