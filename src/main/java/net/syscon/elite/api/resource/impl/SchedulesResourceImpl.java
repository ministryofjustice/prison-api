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

    public SchedulesResourceImpl(final SchedulesService schedulesService) {
        this.schedulesService = schedulesService;
    }

    @Override
    public List<PrisonerSchedule> getGroupEvents(final String agencyId, final String name,
                                                 final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        return schedulesService.getLocationGroupEvents(agencyId, name,
                date, timeSlot, sortFields, sortOrder);
    }

    @Override
    public List<PrisonerSchedule> getLocationEvents(final String agencyId, final Long locationId, final String usage,
                                                    final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        return schedulesService.getLocationEvents(agencyId, locationId, usage,
                date, timeSlot, sortFields, sortOrder);

    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtAllLocations(String agencyId, LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, date, timeSlot, sortFields, sortOrder);
    }

    @Override
    public List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        return schedulesService.getAppointments(agencyId, body, date, timeSlot);

    }

    @Override
    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        return schedulesService.getVisits(agencyId, body, date, timeSlot);
    }

    @Override
    public List<PrisonerSchedule> getActivities(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded) {
        return schedulesService.getActivities(agencyId, body, date, timeSlot, includeExcluded);
    }

    @Override
    public List<PrisonerSchedule> getCourtEvents(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        return schedulesService.getCourtEvents(agencyId, body, date, timeSlot);
    }

    @Override
    public List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> body, final LocalDate date) {
        return schedulesService.getExternalTransfers(agencyId, body, date);
    }
}
