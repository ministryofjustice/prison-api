package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Bookings API repository interface.
 */
public interface BookingRepository {
    Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId);

    List<PrivilegeDetail> getBookingIEPDetails(Long bookingId);

    boolean verifyBookingAccess(Long bookingId, Set<String> agencyIds);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    ScheduledEvent getBookingAppointment(Long bookingId, Long eventId);

    Long createBookingAppointment(Long bookingId, NewAppointment newAppointment, String agencyId);

    Page<OffenderRelease> getOffenderReleaseSummary(LocalDate toReleaseDate, String query, long offset, long limit, String orderByFields, Order order, Set<String> allowedCaseloadsOnly);

    Visit getBookingVisitLast(Long bookingId, LocalDateTime cutoffDate);

    Optional<Long> getBookingIdByOffenderNo(String offenderNo);
}
