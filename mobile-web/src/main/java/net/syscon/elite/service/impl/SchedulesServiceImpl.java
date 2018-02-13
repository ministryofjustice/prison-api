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
    public List<PrisonerSchedule> getLocationGroupTodaysEvents(String agencyId, String groupName, TimeSlot timeSlot) {

        final List<InmateDto> inmates = inmateService.findInmatesByLocation(
                currentUsername(),
                agencyId,
                locationIdsForGroup(agencyId, groupName));

        final LocalDateTime middayToday = middayToday();

        return inmates
                .stream()
                .flatMap(inmate -> prisonerScheduleForInmate(inmate, timeSlot, middayToday))
                .sorted(BY_CELL_LOCATION)
                .collect(Collectors.toList());
    }

    private String currentUsername() {
        return authenticationFacade.getCurrentUsername();
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

    private LocalDateTime middayToday() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
    }

    private Stream<PrisonerSchedule> prisonerScheduleForInmate(InmateDto inmate, TimeSlot timeSlot, LocalDateTime middayToday) {
        return todaysEventsForInmate(inmate)
            .filter( event -> eventStartsInTimeslot(event, timeSlot, middayToday))
            .map(event -> prisonerSchedule(inmate, event));
    }

    private Stream<ScheduledEvent> todaysEventsForInmate(InmateDto inmate) {
        return bookingService.getEventsToday(inmate.getBookingId()).stream();
    }


    private boolean eventStartsInTimeslot(ScheduledEvent event, TimeSlot timeSlot, LocalDateTime middayToday) {
        return timeSlot == null //
                || (timeSlot == TimeSlot.AM && event.getStartTime().isBefore(middayToday))
                || (timeSlot == TimeSlot.PM && !event.getStartTime().isBefore(middayToday));
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
    public List<PrisonerSchedule> getLocationTodaysEvents(String agencyId, Long locationId, String usage,
            TimeSlot timeSlot) {

        validateLocation(locationId);
        validateUsage(usage);
        final LocalDate today = LocalDate.now();
        final LocalDateTime middayToday = middayToday();
        final List<PrisonerSchedule> events;
        switch (usage) {
        case "APP":
            events = scheduleRepository.getLocationAppointments(locationId, today, today, "lastName", Order.ASC);
            break;
        case "VISIT":
            events = scheduleRepository.getLocationVisits(locationId, today, today, "lastName", Order.ASC);
            break;
        default:
            events = scheduleRepository.getLocationActivities(locationId, today, today, "lastName", Order.ASC);
            break;
        }
        if (timeSlot == null) {
            return events;
        }
        return events.stream().filter(p -> (timeSlot == TimeSlot.AM && p.getStartTime().isBefore(middayToday))//
                                        || (timeSlot == TimeSlot.PM && !p.getStartTime().isBefore(middayToday)))//
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
