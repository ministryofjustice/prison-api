package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceAdjustment;

import java.util.List;

public interface OffenderSentenceAdjustmentRepository extends CrudRepository<OffenderSentenceAdjustment, Long> {
    List<OffenderSentenceAdjustment> findAllByOffenderBookId(Long bookingId);
    List<OffenderSentenceAdjustment> findAllByOffenderBookIdAndActiveFlag(Long bookingId, ActiveFlag activeFlag);
}
