package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.resource.ScheduleResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.SchedulesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of /schedules endpoint.
 */
@RestController
@RequestMapping("/schedules")
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
                                                    final LocalDate date, final TimeSlot timeSlot, final String sortFields,
                                                    final Order sortOrder) {

        return schedulesService.getLocationEvents(agencyId, locationId, usage, date, timeSlot, sortFields, sortOrder);

    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtLocation(final Long locationId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder, final boolean includeSuspended) {
        return schedulesService.getActivitiesAtLocation(locationId, date, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtAllLocations(final String agencyId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, date, null, timeSlot, sortFields, sortOrder);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtAllLocationsByDateRange(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, fromDate, toDate, timeSlot, sortFields, sortOrder);
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
