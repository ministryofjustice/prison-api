package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.TimeSlot;

import java.util.List;

/**
 * Schedules API service interface.
 */
public interface SchedulesService {

    List<PrisonerSchedule> getLocationGroupTodaysEvents(String agencyId, String groupName, TimeSlot timeSlot);

    List<PrisonerSchedule> getLocationTodaysEvents(String agencyId, Long locationId, String usage, TimeSlot timeSlot);
}
