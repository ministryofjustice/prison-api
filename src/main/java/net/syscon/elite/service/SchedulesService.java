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

    List<PrisonerSchedule> getLocationGroupEvents(final String agencyId, final String groupName,
                                                  final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder);

    List<PrisonerSchedule> getLocationEvents(final String agencyId, final Long locationId, final String usage,
                                             final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder);

    List<PrisonerSchedule> getActivitiesAtAllLocations(final String agencyId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder);

    List<PrisonerSchedule> getVisits(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot);

    List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot);

    List<PrisonerSchedule> getActivities(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded);

    List<PrisonerSchedule> getCourtEvents(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot);

    List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> offenderNos, final LocalDate date);
}
