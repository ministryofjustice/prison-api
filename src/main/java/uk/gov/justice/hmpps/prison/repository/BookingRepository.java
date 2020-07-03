package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.NewBooking;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalculation;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;
import uk.gov.justice.hmpps.prison.api.model.RecallBooking;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDefaults;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.impl.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Bookings API repository interface.
 */
public interface BookingRepository {
    Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId);

    Map<Long, List<PrivilegeDetail>> getBookingIEPDetailsByBookingIds(List<Long> bookingIds);

    void addIepLevel(Long bookingId, String username, IepLevelAndComment iepLevel);

    Set<String> getIepLevelsForAgencySelectedByBooking(long bookingId);

    Map<Long, List<String>> getAlertCodesForBookings(List<Long> bookingIds, LocalDateTime cutoffDate);

    boolean verifyBookingAccess(Long bookingId, Set<String> agencyIds);

    Optional<String> getBookingAgency(Long bookingId);

    boolean checkBookingExists(Long bookingId);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    List<ScheduledEvent> getBookingActivities(Collection<Long> bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    void updateAttendance(Long bookingId, Long activityId, UpdateAttendance updateAttendance, boolean paid, boolean authorisedAbsence);

    LocalDate getAttendanceEventDate(Long activityId);

    PayableAttendanceOutcomeDto getPayableAttendanceOutcome(String eventType, String outcomeCode);

    Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Optional<VisitBalances> getBookingVisitBalances(Long bookingId);

    List<ScheduledEvent> getBookingVisits(Collection<Long> bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    List<ScheduledEvent> getBookingAppointments(Collection<Long> bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order);

    ScheduledEvent getBookingAppointment(Long bookingId, Long eventId);

    Long createBookingAppointment(Long bookingId, NewAppointment newAppointment, String agencyId);

    List<OffenderSentenceDetailDto> getOffenderSentenceSummary(String query, Set<String> allowedCaseloadsOnly, boolean filterByCaseload, boolean viewInactiveBookings);

    List<OffenderSentenceCalculation> getOffenderSentenceCalculations(Set<String> agencyIds);

    List<OffenderSentenceTerms> getOffenderSentenceTerms(Long bookingId, List<String> sentenceTermCodes);

    VisitDetails getBookingVisitLast(Long bookingId, LocalDateTime cutoffDate);

    VisitDetails getBookingVisitNext(Long bookingId, LocalDateTime from);

    Optional<OffenderBookingIdSeq> getLatestBookingIdentifierForOffender(String offenderNo);

    List<OffenderSummary> getBookingsByRelationship(String externalRef, String relationshipType, String identifierType);

    List<OffenderSummary> getBookingsByRelationship(Long personId, String relationshipType);

    /**
     * Gets summary details of latest offender booking (whether active or not) for offender associated with booking
     * identified by specified booking id.
     *
     * @param bookingId booking id.
     * @return summary details of latest offender booking for offender.
     */
    Optional<OffenderSummary> getLatestBookingByBookingId(Long bookingId);

    /**
     * Gets summary details of latest offender booking (whether active or not) for offender identified by specified
     * offender number.
     *
     * @param offenderNo offender number.
     * @return summary details of latest offender booking for offender.
     */
    Optional<OffenderSummary> getLatestBookingByOffenderNo(String offenderNo);

    Long createBooking(String agencyId, NewBooking newBooking);

    Long recallBooking(String agencyId, RecallBooking recallBooking);

    void createMultipleAppointments(List<AppointmentDetails> flattenedDetails, AppointmentDefaults defaults, String agencyId);

    List<Long> findBookingsIdsInAgency(List<Long> bookingIds, String agencyId);
}
