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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schedules API service implementation.
 */
@Service
@Transactional(readOnly = true)
public class SchedulesServiceImpl implements SchedulesService {
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

        final List<Location> locations = locationService.getGroup(agencyId, groupName);
        final List<PrisonerSchedule> results = new ArrayList<>();
        final LocalDateTime middayToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
        final String currentUsername = authenticationFacade.getCurrentUsername();

        final List<Long> locationIdList = locations.stream().mapToLong(Location::getLocationId).boxed()
                .collect(Collectors.toList());
        final List<InmateDto> inmates = inmateService.findInmatesByLocation(currentUsername, agencyId, locationIdList);
        for (InmateDto inmate : inmates) {
            final List<ScheduledEvent> eventsToday = bookingService.getEventsToday(inmate.getBookingId());
            for (ScheduledEvent event : eventsToday) {
                if (timeSlot == null //
                        || (timeSlot == TimeSlot.AM && event.getStartTime().isBefore(middayToday))
                        || (timeSlot == TimeSlot.PM && !event.getStartTime().isBefore(middayToday))) {
                    final PrisonerSchedule result = PrisonerSchedule.builder()//
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
                    results.add(result);
                }
            }
        }
        return results;
    }

    @Override
    @VerifyAgencyAccess
    public List<PrisonerSchedule> getLocationTodaysEvents(String agencyId, Long locationId, String usage,
            TimeSlot timeSlot) {

        validateLocation(locationId);
        validateUsage(usage);
        final LocalDate today = LocalDate.now();
        final LocalDateTime middayToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
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
