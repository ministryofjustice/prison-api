package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.model.ScheduledAppointmentDto;
import uk.gov.justice.hmpps.prison.api.resource.ScheduleResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;
import uk.gov.justice.hmpps.prison.service.SchedulesService;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of /schedules endpoint.
 */
@RestController
@RequestMapping("${api.base.path}/schedules")
public class SchedulesResourceImpl implements ScheduleResource {
    private final SchedulesService schedulesService;
    private final AppointmentsService appointmentsService;

    public SchedulesResourceImpl(final SchedulesService schedulesService, final AppointmentsService appointmentsService) {
        this.schedulesService = schedulesService;
        this.appointmentsService = appointmentsService;
    }

    @Override
    public List<PrisonerSchedule> getEventsByLocationId(final String agencyId, final List<Long> locationIds,
                                                        final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        return schedulesService.getLocationGroupEventsByLocationId(agencyId, locationIds,
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
    public List<PrisonerSchedule> getActivitiesAtAllLocations(final String agencyId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder, final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, date, null, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtAllLocationsByDateRange(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final TimeSlot timeSlot, final String sortFields, final Order sortOrder, final boolean includeSuspended) {
        return schedulesService.getActivitiesAtAllLocations(agencyId, fromDate, toDate, timeSlot, sortFields, sortOrder, includeSuspended);
    }

    @Override
    public List<PrisonerSchedule> getAppointmentsForOffenders(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        return schedulesService.getAppointments(agencyId, body, date, timeSlot);

    }

    @Override
    public List<ScheduledAppointmentDto> getAppointments(final String agencyId, final LocalDate date, final Long locationId, final TimeSlot timeSlot) {
        return appointmentsService.getAppointments(agencyId, date, locationId, timeSlot);
    }

    @Override
    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot) {
        return schedulesService.getVisits(agencyId, body, date, timeSlot);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesForBookings(final String agencyId, final List<String> body, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded) {
        return schedulesService.getActivitiesByEventIds(agencyId, body, date, timeSlot, includeExcluded);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesByEventIds(final String agencyId, final List<Long> eventIds) {
        return schedulesService.getActivitiesByEventIds(agencyId, eventIds);
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
