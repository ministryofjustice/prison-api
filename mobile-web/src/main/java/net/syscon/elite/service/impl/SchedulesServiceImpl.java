package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.SchedulesService;
import net.syscon.elite.service.support.InmateDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuthenticationFacade authenticationFacade;

    public SchedulesServiceImpl(LocationService locationService, InmateService inmateService,
            BookingService bookingService, AuthenticationFacade authenticationFacade) {
        this.locationService = locationService;
        this.inmateService = inmateService;
        this.bookingService = bookingService;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @VerifyBookingAccess
    public List<PrisonerSchedule> getLocationGroupTodaysEvents(String agencyId, String groupName, TimeSlot timeSlot) {
 
        bookingService.verifyBookingAccess(agencyId);
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
    public List<PrisonerSchedule> getLocationTodaysEvents(String agencyId, Long locationId, String usage,
            TimeSlot timeSlot) {
        final List<ScheduledEvent> events;
        switch (usage) {
        case "APP":
            events = bookingService.getBookingAppointments(locationId, null, null, usage, null);
            break;
        case "VISIT":
            events = bookingService.getBookingVisits(locationId, null, null, usage, null);
            break;
        default:
            events = bookingService.getBookingActivities(locationId, null, null, usage, null);
        }
        return events.stream().map(event -> PrisonerSchedule.builder()//
                .comment(event.getEventSourceDesc())//
                .startTime(event.getStartTime())//
                .endTime(event.getEndTime())//
                .event(event.getEventSubType())//
                .eventDescription(event.getEventSubTypeDesc())//
                .build()).collect(Collectors.toList());
    }
}
