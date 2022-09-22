package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator

interface OffenceIndicatorRepository : CrudRepository<OffenceIndicator, Long> {
  fun deleteByIndicatorCodeAndOffence_Code(indicatorCode: String, offenceCode: String): Long
  fun countByIndicatorCodeAndOffence_Code(indicatorCode: String, offenceCode: String): Long
}
