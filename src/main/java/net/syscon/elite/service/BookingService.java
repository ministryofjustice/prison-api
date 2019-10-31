package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.validation.AttendanceTypesValid;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Bookings API service interface.
 */
public interface BookingService {

    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);

    void addIepLevel(Long bookingId, String username, @Valid IepLevelAndComment iepLevel);

    Map<Long, PrivilegeSummary> getBookingIEPSummary(List<Long> bookings, boolean withDetails);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    void updateAttendance(String offenderNo, Long activityId, @Valid @AttendanceTypesValid UpdateAttendance updateAttendance);

    void updateAttendance(final Long bookingId, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance);

    void updateAttendanceForMultipleBookingIds(final Set<BookingActivity> bookingActivities, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance);

    Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Optional<VisitBalances> getBookingVisitBalances(final Long bookingId);

    Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    ScheduledEvent createBookingAppointment(Long bookingId, String username, @Valid NewAppointment newAppointment);

    void checkBookingExists(Long bookingId);

    List<OffenceDetail> getMainOffenceDetails(Long bookingId);

    List<Offence> getMainOffenceDetails(Set<Long> bookingIds);

    List<OffenceHistoryDetail> getOffenceHistory(String offenderNo);

    List<ScheduledEvent> getEvents(Long bookingId, LocalDate fromDate, LocalDate toDate);

    List<ScheduledEvent> getEventsToday(Long bookingId);

    List<ScheduledEvent> getEventsOnDay(Collection<Long> bookingIds, LocalDate day);

    List<ScheduledEvent> getEventsThisWeek(Long bookingId);

    List<ScheduledEvent> getEventsNextWeek(Long bookingId);

    List<OffenderSentenceCalculation> getOffenderSentenceCalculationsForAgency(Set<String> agencyIds);

    List<OffenderSentenceTerms> getOffenderSentenceTerms(Long bookingId);

    List<OffenderSentenceDetail> getOffenderSentencesSummary(String agencyId, List<String> offenderNos);

    List<OffenderSentenceDetail> getBookingSentencesSummary(List<Long> bookingIds);

    Optional<OffenderSentenceDetail> getOffenderSentenceDetail(final String offenderNo);

    Visit getBookingVisitLast(Long bookingId);

    Visit getBookingVisitNext(Long bookingId);

    List<OffenderSummary> getBookingsByExternalRefAndType(String externalRef, String relationshipType);

    List<OffenderSummary> getBookingsByPersonIdAndType(Long personId, String relationshipType);

    Long getBookingIdByOffenderNo(final String offenderNo, final String... rolesAllowed);

    void verifyBookingAccess(Long bookingId, String... rolesAllowed);

    void verifyCanViewSensitiveBookingInfo(String offenderNo, String... roleAllowed);

    /**
     * <<< FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER >>>
     * <p>
     * Get latest booking summary details for offender associated with booking identified by specified booking id.
     * Booking may be inactive but it will represent the latest known booking for the offender.
     *
     * @param bookingId booking id.
     * @return offender booking summary or {@code null} if there is no offender booking for specified booking id.
     */
    OffenderSummary getLatestBookingByBookingId(Long bookingId);

    /**
     * <<< FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER >>>
     * <p>
     * Get latest booking summary details for offender identified by specified offender number. Booking may be inactive
     * but it will represent the latest known booking for the offender.
     *
     * @param offenderNo offender number.
     * @return offender booking summary or {@code null} if no offender with specified offender number exists or if a
     * latest booking cannot be located for offender.
     */
    OffenderSummary getLatestBookingByOffenderNo(String offenderNo);

    OffenderSummary createBooking(@Valid NewBooking newBooking);

    OffenderSummary recallBooking(@Valid RecallBooking recallBooking);

    Map<Long, List<String>> getBookingAlertSummary(List<Long> bookingIds, LocalDateTime now);
}
