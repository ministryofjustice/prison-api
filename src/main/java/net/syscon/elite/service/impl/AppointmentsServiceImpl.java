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
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Validated
@Transactional
public class AppointmentsServiceImpl implements AppointmentsService {

    // Maximum of 1000 values in an Oracle 'IN' clause is current hard limit. (See #validateBookingIds below).
    private static final int MAXIMUM_NUMBER_OF_APPOINTMENTS = 1000;

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

        enforceMaximumNumberOfAppointments(appointments);

        final var defaults = appointments.getAppointmentDefaults();

        final String agencyId = findLocationInUserLocations(defaults.getLocationId())
                .orElseThrow(() -> new BadRequestException("Location does not exist or is not in your caseload."))
                .getAgencyId();

        assertValidAppointmentType(defaults.getAppointmentType());
        assertAllBookingIdsInCaseload(appointments.getAppointments(), agencyId);

        List<AppointmentDetails> flattenedDetails = appointments.withDefaults();

        assertAdditionalAppointmentConstraints(flattenedDetails);

        createAppointments(repeat(flattenedDetails, appointments.getRepeat()), defaults, agencyId);
    }

    private void enforceMaximumNumberOfAppointments(AppointmentsToCreate appointments) {
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

    static List<AppointmentDetails> repeat(List<AppointmentDetails> details, Repeat repeat) {
        if (repeat == null) return details;
        final Period repeatPeriod = repeat.getRepeatPeriod().getPeriod();
        return IntStream
                .rangeClosed(0, repeat.getCount())
                .boxed()
                .flatMap(i -> details
                        .stream()
                        .map(d -> d
                                .toBuilder()
                                .startTime(addRepeatOffset(d.getStartTime(), repeatPeriod, i))
                                .endTime(addRepeatOffset(d.getEndTime(), repeatPeriod, i))
                                .build()))
                .collect(Collectors.toList());
    }

    private static LocalDateTime addRepeatOffset(LocalDateTime time, Period period, int times) {
        if (time == null) return null;
        return time.plus(period.multipliedBy(times));
    }

    private void createAppointments(List<AppointmentDetails> details, AppointmentDefaults defaults, String agencyId) {
        bookingRepository.createMultipleAppointments(details, defaults, agencyId);
        trackAppointmentsCreated(details, defaults);
    }
}