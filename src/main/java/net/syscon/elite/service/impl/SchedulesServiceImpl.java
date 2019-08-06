package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.ScheduleRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schedules API service implementation.
 */
@Service
@Transactional(readOnly = true)
public class SchedulesServiceImpl implements SchedulesService {
    private static final Comparator<PrisonerSchedule> BY_CELL_LOCATION = Comparator.comparing(PrisonerSchedule::getCellLocation);
    private static final Comparator<PrisonerSchedule> BY_LAST_NAME = Comparator.comparing(PrisonerSchedule::getLastName);

    private final LocationService locationService;
    private final InmateService inmateService;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;
    private final ScheduleRepository scheduleRepository;
    private final AuthenticationFacade authenticationFacade;

    public SchedulesServiceImpl(final LocationService locationService, final InmateService inmateService,
                                final BookingService bookingService, final ReferenceDomainService referenceDomainService,
                                final ScheduleRepository scheduleRepository, final AuthenticationFacade authenticationFacade) {
        this.locationService = locationService;
        this.inmateService = inmateService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
        this.scheduleRepository = scheduleRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationGroupEvents(final String agencyId, final String groupName, final LocalDate date, final TimeSlot timeSlot,
                                                         final String sortFields, final Order sortOrder) {

        final var inmates = inmateService.findInmatesByLocation(
                authenticationFacade.getCurrentUsername(),
                agencyId,
                locationIdsForGroup(agencyId, groupName));

        if (inmates.isEmpty()) {
            return Collections.emptyList();
        }

        final var day = date == null ? LocalDate.now() : date;

        final var prisonerSchedules = prisonerSchedules(inmates, timeSlot, day);

        return prisonerSchedules.stream()
                .sorted(getPrisonerScheduleComparator(sortFields, sortOrder))
                .collect(Collectors.toList());
    }

    private Comparator<PrisonerSchedule> getPrisonerScheduleComparator(final String sortFields, final Order sortOrder) {
        final var orderFields = StringUtils.defaultString(sortFields, "cellLocation");
        var comparator = "cellLocation".equals(orderFields) ? BY_CELL_LOCATION : BY_LAST_NAME;
        comparator = comparator.thenComparing(PrisonerSchedule::getOffenderNo);
        if (sortOrder == Order.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(PrisonerSchedule::getStartTime);
        return comparator;
    }

    private List<Long> locationIdsForGroup(final String agencyId, final String groupName) {
        final var locations = locationService.getCellLocationsForGroup(agencyId, groupName);
        return idsOfLocations(locations);
    }

    private List<Long> idsOfLocations(final List<Location> locations) {
        return locations
                .stream()
                .map(Location::getLocationId)
                .collect(Collectors.toList());
    }

    private List<PrisonerSchedule> prisonerSchedules(final Collection<InmateDto> inmates, final TimeSlot timeSlot, final LocalDate date) {

        final var bookingIdMap =
                inmates.stream().collect(Collectors.toMap(InmateDto::getBookingId, inmateDto -> inmateDto));

        final var eventsOnDay = bookingService.getEventsOnDay(bookingIdMap.keySet(), date);

        return eventsOnDay.stream()
                .filter(event -> CalcDateRanges.eventStartsInTimeslot(event.getStartTime(), timeSlot))
                .map(event -> prisonerSchedule(bookingIdMap.get(event.getBookingId()), event)).collect(Collectors.toList());
    }

    private PrisonerSchedule prisonerSchedule(final InmateDto inmate, final ScheduledEvent event) {
        return PrisonerSchedule.builder()
                .bookingId(inmate.getBookingId())
                .locationId(event.getEventLocationId())
                .cellLocation(inmate.getLocationDescription())
                .lastName(inmate.getLastName())
                .firstName(inmate.getFirstName())
                .offenderNo(inmate.getOffenderNo())
                .comment(event.getEventSourceDesc())
                .endTime(event.getEndTime())
                .event(event.getEventSubType())
                .eventType(event.getEventType())
                .eventDescription(event.getEventSubTypeDesc())
                .startTime(event.getStartTime())
                .eventId(event.getEventId())
                .eventOutcome(event.getEventOutcome())
                .performance(event.getPerformance())
                .outcomeComment(event.getOutcomeComment())
                .paid(event.getPaid())
                .payRate(event.getPayRate())
                .eventStatus(event.getEventStatus())
                .eventLocation(WordUtils.capitalizeFully(event.getEventLocation()))
                .eventLocationId(event.getEventLocationId())
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationEvents(final String agencyId, final Long locationId, final String usage,
                                                    final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        validateLocation(locationId);
        validateUsage(usage);
        final var day = date == null ? LocalDate.now() : date;
        final var events = getPrisonerSchedules(locationId, usage, sortFields, sortOrder, day);
        return filterByTimeSlot(timeSlot, events);
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getActivitiesAtAllLocations(final String agencyId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        final var day = date == null ? LocalDate.now() : date;
        final var orderByFields = StringUtils.defaultString(sortFields, "lastName");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        final var activities =  scheduleRepository.getAllActivitiesAtAgency(agencyId, day, day, orderByFields, order);

        return filterByTimeSlot(timeSlot, activities);
    }

    private List<PrisonerSchedule> getPrisonerSchedules(final Long locationId, final String usage, final String sortFields, final Order sortOrder, final LocalDate day) {
        final var orderByFields = StringUtils.defaultString(sortFields, "lastName");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);
        switch (usage) {
            case "APP":
                return scheduleRepository.getLocationAppointments(locationId, day, day, orderByFields, order);
            case "VISIT":
                return scheduleRepository.getLocationVisits(locationId, day, day, orderByFields, order);
            default:
                return scheduleRepository.getLocationActivities(locationId, day, day, orderByFields, order);
        }
    }

    @Override
    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> offenderNo, final LocalDate date, final TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNo.isEmpty()) {
            return Collections.emptyList();
        }

        final var visits = scheduleRepository.getVisits(agencyId, offenderNo, date);

        return filterByTimeSlot(timeSlot, visits);
    }

    @Override
    public List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> offenderNo, final LocalDate date, final TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNo.isEmpty()) {
            return Collections.emptyList();
        }

        final var appointments = scheduleRepository.getAppointments(agencyId, offenderNo, date);

        return filterByTimeSlot(timeSlot, appointments);
    }

    @Override
    public List<PrisonerSchedule> getActivities(final String agencyId, final List<String> offenderNumbers, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        final var activities = scheduleRepository.getActivities(agencyId, offenderNumbers, date);

        final var filtered = filterByTimeSlot(timeSlot, activities);
        if (includeExcluded) {
            return filtered;
        }
        return filtered.stream().filter(ps -> !ps.getExcluded()).collect(Collectors.toList());
    }

    @Override
    public List<PrisonerSchedule> getCourtEvents(final String agencyId, final List<String> offenderNumbers, final LocalDate date, final TimeSlot timeSlot) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        final var events = scheduleRepository.getCourtEvents(offenderNumbers, date);

        return filterByTimeSlot(timeSlot, events);
    }

    @Override
    public List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> offenderNumbers, final LocalDate date) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        return scheduleRepository.getExternalTransfers(agencyId, offenderNumbers, date);
    }

    private List<PrisonerSchedule> filterByTimeSlot(final TimeSlot timeSlot, final List<PrisonerSchedule> events) {

        if (timeSlot == null) {
            return events;
        }

        return events.stream()
                .filter(p -> CalcDateRanges.eventStartsInTimeslot(p.getStartTime(), timeSlot))
                .collect(Collectors.toList());
    }

    private void validateLocation(final Long locationId) {
        locationService.getLocation(locationId);
    }

    private void validateUsage(final String usage) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(),
                    usage, false);
        } catch (final EntityNotFoundException ex) {
            throw new BadRequestException("Usage not recognised.");
        }
    }
}
