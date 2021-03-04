package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.Optional;

public interface OffenderAssessmentRepository extends CrudRepository<OffenderAssessment, OffenderAssessment.Pk> {
    Optional<OffenderAssessment> findByBookingIdAndAssessmentSeq(Long bookingId, Long assessmentSeq);
}
