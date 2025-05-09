package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {

    Optional<CourtEvent> findByOffenderBooking_BookingIdAndId(final Long bookingId, final Long eventId);
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

    List<CourtEvent> findByOffenderBooking_BookingIdInAndOffenderCourtCase_CaseStatus_Code(Set<Long> bookingIds, String caseStatusCode);

    Optional<CourtEvent> findFirstByOffenderBooking_BookingIdAndStartTimeGreaterThanEqual(Long bookingId, LocalDateTime earliestTime, Sort sort);
}
