package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.CourtEventOutcome;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {

    List<CourtEvent> findByOffenderBooking_BookingIdOrderByIdAsc(final Long bookingId);

    @Query("select " +
            "o.nomsId as offenderNo, " +
            "ce.courtLocation.id as court, " +
            "ce.courtLocation.description as courtDescription, " +
            "ce.startTime as startTime, " +
            "ce.courtEventType.code as eventSubType, " +
            "ce.courtEventType.description as eventDescription, " +
            "ce.holdFlag as holdFlag " +
            "from CourtEvent ce " +
            "join ce.offenderBooking ob " +
            "join ob.offender o " +
            "where ce.startTime >= :cutoffDate")
    List<Map<String, Object>> getCourtEventsUpcoming(@Param("cutoffDate") LocalDateTime cutoff);

    Optional<CourtEvent> findOneByOffenderBookingBookingIdAndParentCourtEventId(Long bookingId, Long parentCourtEventId);

    @Query("""
    SELECT
      e.offenderBooking.bookingId,
      e.id,
      e.outcomeReasonCode.code
    FROM CourtEvent e
    WHERE e.offenderBooking.bookingId IN :bookingIds
      AND e.offenderCourtCase.caseStatus.code = :caseStatusCode
      AND e.outcomeReasonCode.code IN :outcomeReasonCodes
""")
    List<CourtEventOutcome> findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(
        @Param("bookingIds") Set<Long> bookingIds,
        @Param("caseStatusCode") String caseStatusCode,
        @Param("outcomeReasonCodes") Set<String> outcomeReasonCodes
    );

    @Query("""
    SELECT
      e.offenderBooking.bookingId,
      e.id,
      e.outcomeReasonCode.code
    FROM CourtEvent e
    WHERE e.offenderBooking.bookingId IN :bookingIds
      AND e.offenderCourtCase.caseStatus.code = :caseStatusCode
""")
    List<CourtEventOutcome> findCourtEventOutcomesByBookingIdsAndCaseStatus(
        @Param("bookingIds") Set<Long> bookingIds,
        @Param("caseStatusCode") String caseStatusCode
    );

    Optional<CourtEvent> findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(Long bookingId, LocalDateTime earliestTime, Sort sort);
}
