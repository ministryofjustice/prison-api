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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public SchedulesServiceImpl(LocationService locationService, InmateService inmateService,
                                BookingService bookingService, ReferenceDomainService referenceDomainService,
                                ScheduleRepository scheduleRepository, AuthenticationFacade authenticationFacade) {
        this.locationService = locationService;
        this.inmateService = inmateService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
        this.scheduleRepository = scheduleRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationGroupEvents(String agencyId, String groupName, LocalDate date, TimeSlot timeSlot,
                                                         String sortFields, Order sortOrder) {

        final List<InmateDto> inmates = inmateService.findInmatesByLocation(
                authenticationFacade.getCurrentUsername(),
                agencyId,
                locationIdsForGroup(agencyId, groupName));
        final LocalDate day = date == null ? LocalDate.now() : date;
        final LocalDateTime midday = midday(day);
        final LocalDateTime evening = evening(day);

        final String orderFields = StringUtils.defaultString(sortFields, "cellLocation");
        Comparator<PrisonerSchedule> comparator = "cellLocation".equals(orderFields) ? BY_CELL_LOCATION : BY_LAST_NAME;
        if (sortOrder == Order.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(PrisonerSchedule::getStartTime);

        return inmates.stream()
                .flatMap(inmate -> prisonerScheduleForInmate(inmate, timeSlot, day, midday, evening))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Long> locationIdsForGroup(String agencyId, String groupName) {
        final List<Location> locations = locationService.getGroup(agencyId, groupName);
        return idsOfLocations(locations);
    }

    private List<Long> idsOfLocations(List<Location> locations) {
        return locations
                .stream()
                .mapToLong(Location::getLocationId)
                .boxed()
                .collect(Collectors.toList());
    }

    private LocalDateTime midday(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(12, 0));
    }

    private LocalDateTime evening(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(17, 0));
    }

    private Stream<PrisonerSchedule> prisonerScheduleForInmate(InmateDto inmate, TimeSlot timeSlot, LocalDate date, LocalDateTime midday, LocalDateTime evening) {
        final List<ScheduledEvent> eventsOnDay = bookingService.getEventsOnDay(inmate.getBookingId(), date);
        return eventsOnDay.stream()
                .filter(event -> eventStartsInTimeslot(event.getStartTime(), timeSlot, midday, evening))
                .map(event -> prisonerSchedule(inmate, event));
    }

    private boolean eventStartsInTimeslot(LocalDateTime start, TimeSlot timeSlot,
                                          LocalDateTime midday, LocalDateTime evening) {
        return timeSlot == null
                || (timeSlot == TimeSlot.AM && start.isBefore(midday))
                || (timeSlot == TimeSlot.PM && !start.isBefore(midday) && start.isBefore(evening))
                || (timeSlot == TimeSlot.ED && !start.isBefore(evening));
    }

    private PrisonerSchedule prisonerSchedule(InmateDto inmate, ScheduledEvent event) {
        return PrisonerSchedule.builder()//
                .cellLocation(inmate.getLocationDescription())//
                .lastName(inmate.getLastName())//
                .firstName(inmate.getFirstName())//
                .offenderNo(inmate.getOffenderNo())//
                .comment(event.getEventSourceDesc())//
                .endTime(event.getEndTime())//
                .event(event.getEventSubType())//
                .eventDescription(event.getEventSubTypeDesc())//
                .startTime(event.getStartTime())//
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationEvents(String agencyId, Long locationId, String usage,
                                                    LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder) {

        validateLocation(locationId);
        validateUsage(usage);
        final LocalDate day = date == null ? LocalDate.now() : date;
        final LocalDateTime midday = midday(day);
        final LocalDateTime evening = evening(day);
        final List<PrisonerSchedule> events;
        final String orderByFields = StringUtils.defaultString(sortFields, "lastName");
        final Order order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);
        switch (usage) {
            case "APP":
                events = scheduleRepository.getLocationAppointments(locationId, day, day, orderByFields, order);
                break;
            case "VISIT":
                events = scheduleRepository.getLocationVisits(locationId, day, day, orderByFields, order);
                break;
            default:
                events = scheduleRepository.getLocationActivities(locationId, day, day, orderByFields, order);
                break;
        }
        if (timeSlot == null) {
            return events;
        }
        return events.stream()
                .filter(p -> eventStartsInTimeslot(p.getStartTime(), timeSlot, midday, evening))
                .collect(Collectors.toList());
    }

    private void validateLocation(Long locationId) {
        locationService.getLocation(locationId);
    }

    private void validateUsage(String usage) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(),
                    usage, false);
        } catch (EntityNotFoundException ex) {
            throw new BadRequestException("Usage not recognised.");
        }
    }
}
