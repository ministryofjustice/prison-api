package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;

import java.util.List;
import java.util.Optional;

public interface OffenderAssessmentRepository extends CrudRepository<OffenderAssessment, OffenderAssessment.Pk> {
    Optional<OffenderAssessment> findByBookingIdAndAssessmentSeq(Long bookingId, Integer assessmentSeq);

    @Query("""
        SELECT oa FROM OffenderAssessment oa 
        INNER JOIN oa.offenderBooking booking INNER JOIN booking.offender offender 
        INNER JOIN oa.assessmentType assessment
        WHERE offender.nomsId = :offenderNo AND assessment.cellSharingAlertFlag = 'Y'
        ORDER BY oa.assessmentDate DESC, oa.assessmentSeq DESC
        """)
    List<OffenderAssessment> findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst(String offenderNo);

    @Query("""
        SELECT oa FROM OffenderAssessment oa 
        INNER JOIN oa.offenderBooking booking INNER JOIN booking.offender offender 
        INNER JOIN oa.assessmentType assessment
        WHERE offender.nomsId IN (:offenderNos) AND assessment.cellSharingAlertFlag = 'Y'
        ORDER BY oa.assessmentDate DESC, oa.assessmentSeq DESC
        """)
    List<OffenderAssessment> findByCsraAssessmentAndByOffenderNos_OrderByLatestFirst(List<String> offenderNos);
}
