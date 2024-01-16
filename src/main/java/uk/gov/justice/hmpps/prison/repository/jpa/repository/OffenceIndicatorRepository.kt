package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator

interface OffenceIndicatorRepository : CrudRepository<OffenceIndicator, Long> {
  fun deleteByIndicatorCodeAndOffenceCode(indicatorCode: String, offenceCode: String): Long
  fun existsByIndicatorCodeAndOffenceCode(indicatorCode: String, offenceCode: String): Boolean
}
