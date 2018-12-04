package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.resource.ScheduleResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.SchedulesService;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of /schedules endpoint.
 */
@RestResource
@Path("/schedules")
public class SchedulesResourceImpl implements ScheduleResource {
    private final SchedulesService schedulesService;

    public SchedulesResourceImpl(SchedulesService schedulesService) {
        this.schedulesService = schedulesService;
    }

    @Override
    public GetGroupEventsResponse getGroupEvents(String agencyId, String name,
                                                 LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder) {

        final List<PrisonerSchedule> events = schedulesService.getLocationGroupEvents(agencyId, name,
                date, timeSlot, sortFields, sortOrder);

        return GetGroupEventsResponse.respond200WithApplicationJson(events);
    }

    @Override
    public GetLocationEventsResponse getLocationEvents(String agencyId, Long locationId, String usage,
                                                       LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder) {

        final List<PrisonerSchedule> events = schedulesService.getLocationEvents(agencyId, locationId, usage,
                date, timeSlot, sortFields, sortOrder);

        return GetLocationEventsResponse.respond200WithApplicationJson(events);
    }

    @Override
    public GetAppointmentsResponse getAppointments(String agencyId, List<String> body, LocalDate date, TimeSlot timeSlot) {
        final List<PrisonerSchedule> appointments = schedulesService.getAppointments(agencyId, body, date, timeSlot);

        return GetAppointmentsResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetVisitsResponse getVisits(String agencyId, List<String> body, LocalDate date, TimeSlot timeSlot) {
        final List<PrisonerSchedule> visits = schedulesService.getVisits(agencyId, body, date, timeSlot);

        return GetVisitsResponse.respond200WithApplicationJson(visits);
    }

    @Override
    public GetActivitiesResponse getActivities(String agencyId, List<String> body, LocalDate date, TimeSlot timeSlot, boolean includeExcluded) {
        final List<PrisonerSchedule> activities = schedulesService.getActivities(agencyId, body, date, timeSlot, includeExcluded);
        return GetActivitiesResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetCourtEventsResponse getCourtEvents(String agencyId, List<String> body, LocalDate date, TimeSlot timeSlot) {
        final List<PrisonerSchedule> activities = schedulesService.getCourtEvents(agencyId, body, date, timeSlot);
        return GetCourtEventsResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetExternalTransfersResponse getExternalTransfers(String agencyId, List<String> body, LocalDate date) {
        final List<PrisonerSchedule> transfers = schedulesService.getExternalTransfers(agencyId, body, date);

        return GetExternalTransfersResponse.respond200WithApplicationJson(transfers);
    }
}
