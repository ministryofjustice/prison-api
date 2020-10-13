package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {

    Optional<CourtEvent> findByOffenderBooking_BookingIdAndId(final Long bookingId, final Long eventId);

    @Query("select " +
            "o.nomsId as offenderNo, " +
            "ce.courtLocation.id as court, " +
            "ce.courtLocation.description as courtDescription, " +
            "ce.startTime as startTime, " +
            "ce.courtEventType.code as eventSubType, " +
            "ce.courtEventType.description as eventDescription, " +
            "ce.holdFlag as hold " +
            "from CourtEvent ce " +
            "join ce.offenderBooking ob " +
            "join ob.offender o " +
            "where ce.startTime >= :cutoffDate")
    List<Map> getCourtEventsUpcoming(@Param("cutoffDate") LocalDateTime cutoff);
}
