package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.resource.ScheduleResource;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.SchedulesService;

import javax.ws.rs.Path;

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
    public GetSchedulesAgencyIdGroupsNameResponse getSchedulesAgencyIdGroupsName(String agencyId, String name,
            TimeSlot timeSlot) {

        final List<PrisonerSchedule> events = schedulesService.getLocationGroupTodaysEvents(agencyId, name, timeSlot);

        return GetSchedulesAgencyIdGroupsNameResponse.respond200WithApplicationJson(events);
    }

    @Override
    public GetSchedulesAgencyIdLocationLocationIdUsageUsageResponse getSchedulesAgencyIdLocationLocationIdUsageUsage(
            String agencyId, Long locationId, String usage, TimeSlot timeSlot) {

        final List<PrisonerSchedule> events = schedulesService.getLocationTodaysEvents(agencyId, locationId, usage, timeSlot);

        return GetSchedulesAgencyIdLocationLocationIdUsageUsageResponse.respond200WithApplicationJson(events);
    }
}
