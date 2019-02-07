package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.api.model.bulkappointments.AppointmentToCreate;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AppointmentsService;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.util.DateTimeConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class AppointmentsServiceImpl implements AppointmentsService {

    private final BookingRepository bookingRepository;
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final ReferenceDomainService referenceDomainService;

    public AppointmentsServiceImpl(
            BookingRepository bookingRepository,
            AuthenticationFacade authenticationFacade,
            LocationService locationService,
            ReferenceDomainService referenceDomainService) {
        this.bookingRepository = bookingRepository;
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.referenceDomainService = referenceDomainService;
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
        final var defaults = appointments.getAppointmentDefaults();

        Location location = findValidLocation(authenticationFacade.getCurrentUsername(), defaults.getLocationId())
                .orElseThrow(() -> new BadRequestException("Location does not exist or is not in your caseload."));

        validateAppointmentType(defaults);

        validateBookingIds(appointments.getAppointments(), location.getAgencyId());

        List<AppointmentToCreate> appointmentsToCreate = appointments.flatten(location.getAgencyId());

        validateAppointmentTimes(appointmentsToCreate);

        bookingRepository.createMultipleAppointments(appointmentsToCreate);
    }

    private void validateBookingIds(List<AppointmentDetails> appointments, String agencyId) {
        List<Long> bookingIds = appointments.stream().map(AppointmentDetails::getBookingId).collect(Collectors.toList());
        List<Long> bookingIdsInAgency = bookingRepository.findBookingsIdsInAgency(bookingIds, agencyId);
        if (bookingIdsInAgency.size() < bookingIds.size()) {
            throw new BadRequestException("A BookingId does not exist in your caseload");
        }
    }

    private void validateAppointmentTimes(List<AppointmentToCreate> appointments) {
        final Timestamp now = DateTimeConverter.fromLocalDateTime(LocalDateTime.now());

        appointments.forEach(appointment -> {
            validateStartTime(now, appointment);
            validateEndTime(appointment);
        });
    }

    private void validateStartTime(Timestamp now, AppointmentToCreate appointment) {
        if (appointment.getStartTime().before(now)) {
            throw new BadRequestException("Appointment time is in the past.");
        }
    }

    private void validateEndTime(AppointmentToCreate appointment) {
        if (appointment.getEndTime() != null
                && appointment.getEndTime().before(appointment.getStartTime())) {
            throw new BadRequestException("Appointment end time is before the start time.");
        }
    }

    private void validateAppointmentType(AppointmentDefaults defaults) {
        findEventType(defaults.getAppointmentType()).orElseThrow(() -> new BadRequestException("Event type not recognised."));
    }

    private Optional<ReferenceCode> findEventType(String appointmentType) {
        return referenceDomainService.getReferenceCodeByDomainAndCode(
                ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(),
                appointmentType,
                false);
    }

    private Optional<Location> findValidLocation(String username, long locationId) {
        Location appointmentLocation = locationService.getLocation(locationId);
        List<Location> userLocations = locationService.getUserLocations(username);

        for (Location location : userLocations) {
            if (location.getAgencyId().equals(appointmentLocation.getAgencyId())) {
                return Optional.of(location);
            }
        }
        return Optional.empty();
    }
}