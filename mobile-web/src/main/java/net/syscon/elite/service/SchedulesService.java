package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * Schedules API service interface.
 */
public interface SchedulesService {

    List<PrisonerSchedule> getLocationGroupEvents(String agencyId, String groupName,
                                                  LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder);

    List<PrisonerSchedule> getLocationEvents(String agencyId, Long locationId, String usage,
                                             LocalDate date, TimeSlot timeSlot, String sortFields, Order sortOrder);

    List<PrisonerSchedule> getVisits(String agencyId,List<String> offenderNo, LocalDate date, TimeSlot timeSlot);

    List<PrisonerSchedule> getAppointments(String agencyId, List<String> offenderNo, LocalDate date, TimeSlot timeSlot);
}
