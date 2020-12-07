package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderKeyDateAdjustment;

import java.util.List;

public interface OffenderKeyDateAdjustmentRepository extends CrudRepository<OffenderKeyDateAdjustment, Long> {
    List<OffenderKeyDateAdjustment> findAllByOffenderBookId(Long bookingId);
    List<OffenderKeyDateAdjustment> findAllByOffenderBookIdAndActiveFlag(Long bookingId, ActiveFlag activeFlag);
}
