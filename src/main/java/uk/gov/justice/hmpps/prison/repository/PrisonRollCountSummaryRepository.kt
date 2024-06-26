package uk.gov.justice.hmpps.prison.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.PrisonRollCountSummary

@Repository
interface PrisonRollCountSummaryRepository : JpaRepository<PrisonRollCountSummary, Long> {
  fun findAllByPrisonIdAndLocationTypeInAndCertified(
    prisonId: String,
    locationType: List<String>,
    certified: String,
  ): List<PrisonRollCountSummary>

  fun findAllByPrisonIdAndCertified(
    prisonId: String,
    certified: String,
  ): List<PrisonRollCountSummary>

  fun findAllByPrisonId(prisonId: String): List<PrisonRollCountSummary>

  fun findAllByPrisonIdAndParentLocationIdIsNull(prisonId: String): List<PrisonRollCountSummary>
}
