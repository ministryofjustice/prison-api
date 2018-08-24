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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Filter;
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
        if(!inmates.isEmpty()) {

            final LocalDate day = date == null ? LocalDate.now() : date;

            final String orderFields = StringUtils.defaultString(sortFields, "cellLocation");
            Comparator<PrisonerSchedule> comparator = "cellLocation".equals(orderFields) ? BY_CELL_LOCATION : BY_LAST_NAME;
            comparator = comparator.thenComparing(PrisonerSchedule::getOffenderNo);
            if (sortOrder == Order.DESC) {
                comparator = comparator.reversed();
            }
            comparator = comparator.thenComparing(PrisonerSchedule::getStartTime);

            final List<PrisonerSchedule> prisonerSchedules = prisonerSchedules(inmates, timeSlot, day);

            return prisonerSchedules.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
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

    private List<PrisonerSchedule> prisonerSchedules(Collection<InmateDto> inmates, TimeSlot timeSlot, LocalDate date) {

        Map<Long, InmateDto> bookingIdMap =
                inmates.stream().collect(Collectors.toMap(InmateDto::getBookingId, inmateDto -> inmateDto));

        final List<ScheduledEvent> eventsOnDay = bookingService.getEventsOnDay(bookingIdMap.keySet(), date);

        return eventsOnDay.stream()
                .filter(event -> CalcDateRanges.eventStartsInTimeslot(event.getStartTime(), timeSlot))
                .map(event -> prisonerSchedule(bookingIdMap.get(event.getBookingId()), event)).collect(Collectors.toList());
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
                .eventType(event.getEventType())
                .eventDescription(event.getEventSubTypeDesc())//
                .startTime(event.getStartTime())//
                .eventId(event.getEventId())
                .eventOutcome(event.getEventOutcome())
                .performance(event.getPerformance())
                .outcomeComment(event.getOutcomeComment())
                .paid(event.getPaid())
                .payRate(event.getPayRate())
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationEvents(String agencyId, Long locationId, String usage,
                                                    LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder) {

        validateLocation(locationId);
        validateUsage(usage);
        final LocalDate day = date == null ? LocalDate.now() : date;
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

        return FilterByTimeSlot(timeSlot, events);
    }

    @Override
    public List<PrisonerSchedule> getVisits(String agencyId,List<String> offenderNo, LocalDate date, TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNo.isEmpty()) {
            return Collections.emptyList();
        }

        List<PrisonerSchedule> visits = scheduleRepository.getVisits(agencyId, offenderNo, date);

        return FilterByTimeSlot(timeSlot, visits);
    }

    @Override
    public List<PrisonerSchedule> getAppointments(String agencyId, List<String> offenderNo, LocalDate date, TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNo.isEmpty()) {
            return Collections.emptyList();
        }

        List<PrisonerSchedule> appointments = scheduleRepository.getAppointments(agencyId, offenderNo, date);

        return FilterByTimeSlot(timeSlot, appointments);
    }

    @Override
    public List<PrisonerSchedule> getActivities(String agencyId, List<String> offenderNumbers, LocalDate date, TimeSlot timeSlot) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNumbers.isEmpty()) {
            return Collections.emptyList();
        }

        List<PrisonerSchedule> appointments = scheduleRepository.getActivities(agencyId, offenderNumbers, date);

        return FilterByTimeSlot(timeSlot, appointments);
    }

    private List<PrisonerSchedule> FilterByTimeSlot(TimeSlot timeSlot, List<PrisonerSchedule> events) {

        if (timeSlot == null) {
            return events;
        }

        return events.stream()
                .filter(p -> CalcDateRanges.eventStartsInTimeslot(p.getStartTime(), timeSlot))
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
