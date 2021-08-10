package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;

import java.util.List;

public interface OffenderSentenceAdjustmentRepository extends CrudRepository<SentenceAdjustment, Long> {
    List<SentenceAdjustment> findAllByOffenderBooking_BookingId(Long bookingId);
    List<SentenceAdjustment> findAllByOffenderBooking_BookingIdAndActiveFlag(Long bookingId, ActiveFlag activeFlag);
}
