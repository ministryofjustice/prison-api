package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MisStopPointRepository
import java.time.LocalDate

@Service
class DatabaseRestoreInfoService(private val misStopPointRepository: MisStopPointRepository) {
  private companion object {
    private val log = LoggerFactory.getLogger(DatabaseRestoreInfoService::class.java)
  }
  /**
   * We can use the earliest min stop point date - 1 day to determine when the database was last restored.
   * For environments other than pre-production this table might not be readable by the current user so will return null.
   * */
  fun getLastRestoreDate(): LocalDate? = try {
    misStopPointRepository.findMinStopPointDate()?.toLocalDate()?.minusDays(1)
  } catch (e: DataAccessException) {
    log.debug("Caught {} trying to find out the min stop point date - to be expected if not pre-production: {}", e.javaClass.name, e.message)
    null
  }
}
