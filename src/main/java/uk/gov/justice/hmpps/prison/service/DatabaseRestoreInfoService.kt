package uk.gov.justice.hmpps.prison.service

import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MisStopPointRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.RefreshMetadataRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DatabaseRestoreInfoService(
  private val misStopPointRepository: MisStopPointRepository,
  private val refreshMetadataRepository: RefreshMetadataRepository,
) {
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

  fun getLastRestoreDetails(): BackupRestoreDetails? = try {
    refreshMetadataRepository.findAll()
      .maxByOrNull { it.lastRefreshDate }
      ?.let { BackupRestoreDetails(backup = it.sourceBackupDate, restore = it.lastRefreshDate) }
  } catch (e: DataAccessException) {
    log.debug(
      "Caught {} trying to get backup info - to be expected if not pre-production: {}",
      e.javaClass.name,
      e.message,
    )
    null
  }
}

@Schema(description = "Timestamp info for the most recent prod backup and preprod restore")
data class BackupRestoreDetails(
  @Schema(description = "Timestamp of the most recent backup of Nomis production")
  val backup: LocalDateTime,
  @Schema(description = "Timestamp of the most recent restore to preprod")
  val restore: LocalDateTime,
)
