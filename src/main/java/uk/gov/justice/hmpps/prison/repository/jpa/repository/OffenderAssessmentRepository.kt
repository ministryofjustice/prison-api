package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment
import java.util.Optional

interface OffenderAssessmentRepository : CrudRepository<OffenderAssessment, OffenderAssessment.Pk> {
  fun findByBookingIdAndAssessmentSeq(bookingId: Long, assessmentSeq: Int): Optional<OffenderAssessment>

  fun findByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(offenderNos: List<String>, cellSharingFlag: String = "Y"): List<OffenderAssessment>

  @EntityGraph(type = FETCH, value = "offender-assessment-with-details")
  fun findWithDetailsByOffenderBookingOffenderNomsIdInAndAssessmentTypeCellSharingAlertFlagOrderByAssessmentDateDescAssessmentSeqDesc(offenderNos: List<String>, cellSharingFlag: String = "Y"): List<OffenderAssessment>
}
