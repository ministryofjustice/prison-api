package net.syscon.elite.api.resource.impl;

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

    public SchedulesResourceImpl(final SchedulesService schedulesService) {
        this.schedulesService = schedulesService;
    }

    @Override
    public GetGroupEventsResponse getGroupEvents(final String agencyId, final String name,
                                                 final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        final var events = schedulesService.getLocationGroupEvents(agencyId, name,
                date, timeSlot, sortFields, sortOrder);

        return GetGroupEventsResponse.respond200WithApplicationJson(events);
    }

    @Override
    public GetLocationEventsResponse getLocationEvents(final String agencyId, final Long locationId, final String usage,
                                                       final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        final var events = schedulesService.getLocationEvents(agencyId, locationId, usage,
                date, timeSlot, sortFields, sortOrder);

        return GetLocationEventsResponse.respond200WithApplicationJson(events);
    }

    @Override
    public GetAppointmentsResponse getAppointments(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        final var appointments = schedulesService.getAppointments(agencyId, body, date, timeSlot);

        return GetAppointmentsResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetVisitsResponse getVisits(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        final var visits = schedulesService.getVisits(agencyId, body, date, timeSlot);

        return GetVisitsResponse.respond200WithApplicationJson(visits);
    }

    @Override
    public GetActivitiesResponse getActivities(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded) {
        final var activities = schedulesService.getActivities(agencyId, body, date, timeSlot, includeExcluded);
        return GetActivitiesResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetCourtEventsResponse getCourtEvents(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        final var activities = schedulesService.getCourtEvents(agencyId, body, date, timeSlot);
        return GetCourtEventsResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetExternalTransfersResponse getExternalTransfers(final String agencyId, final List<String> body, final LocalDate date) {
        final var transfers = schedulesService.getExternalTransfers(agencyId, body, date);

        return GetExternalTransfersResponse.respond200WithApplicationJson(transfers);
    }
}
