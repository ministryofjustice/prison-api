package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment
import java.util.Optional

interface OffenderAssessmentRepository : CrudRepository<OffenderAssessment, OffenderAssessment.Pk> {
  fun findByBookingIdAndAssessmentSeq(bookingId: Long, assessmentSeq: Int): Optional<OffenderAssessment>

  @Query(
    """
        SELECT
          oa FROM OffenderAssessment oa 
          INNER JOIN oa.offenderBooking booking
          INNER JOIN booking.offender offender 
          INNER JOIN oa.assessmentType assessment
        WHERE
          offender.nomsId IN (:offenderNos) AND assessment.cellSharingAlertFlag = 'Y'
        ORDER BY oa.assessmentDate DESC, oa.assessmentSeq DESC
        """,
  )
  fun findByCsraAssessmentAndByOffenderNosOrderByLatestFirst(offenderNos: List<String>): List<OffenderAssessment>
}
