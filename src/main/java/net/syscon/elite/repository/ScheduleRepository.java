package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository {

    List<PrisonerSchedule> getLocationAppointments(Long locationId, LocalDate fromDate, LocalDate toDate,
                                                   String orderByFields, Order order);

    List<PrisonerSchedule> getLocationVisits(Long locationId, LocalDate fromDate, LocalDate toDate,
                                             String orderByFields, Order order);

    List<PrisonerSchedule> getAllActivitiesAtAgency(String agencyId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order,  boolean includeSuspended);

    List<PrisonerSchedule> getActivitiesAtLocation(Long locationId, LocalDate fromDate, LocalDate toDate,
                                                   String orderByFields, Order order, boolean includeSuspended);

    List<PrisonerSchedule> getVisits(String agencyId, List<String> offenderNo, LocalDate date);

    List<PrisonerSchedule> getAppointments(String agencyId, List<String> offenderNo, LocalDate date);

    List<PrisonerSchedule> getActivities(String agencyId, List<String> offenderNumbers, LocalDate date);

    List<PrisonerSchedule> getCourtEvents(List<String> offenderNumbers, LocalDate date);

    List<PrisonerSchedule> getExternalTransfers(String agencyId, List<String> offenderNumbers, LocalDate date);
}
