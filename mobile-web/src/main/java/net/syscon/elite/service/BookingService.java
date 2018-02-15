package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * Bookings API service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    ScheduledEvent createBookingAppointment(Long bookingId, String username, @Valid NewAppointment newAppointment);

    void verifyBookingAccess(Long bookingId);

    String getBookingAgency(Long bookingId);

    void checkBookingExists(Long bookingId);

    boolean isSystemUser();

    List<OffenceDetail> getMainOffenceDetails(Long bookingId);

    List<ScheduledEvent> getEventsToday(Long bookingId);

    List<ScheduledEvent> getEventsThisWeek(Long bookingId);

    List<ScheduledEvent> getEventsNextWeek(Long bookingId);

    Page<OffenderSentenceDetail> getOffenderSentencesSummary(String username, String query, long offset, long limit, String orderByFields, Order order);

    Visit getBookingVisitLast(Long bookingId);

    List<OffenderSummary> getBookingsByExternalRefAndType(String externalRef, String relationshipType);

    List<OffenderSummary> getBookingsByPersonIdAndType(Long personId, String relationshipType);

    Long getBookingIdByOffenderNo(String offenderNo);
}
