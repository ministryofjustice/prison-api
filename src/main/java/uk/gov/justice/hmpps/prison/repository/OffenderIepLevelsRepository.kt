package uk.gov.justice.hmpps.prison.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderIepLevelsProcs.CreateOffenderIepLevels
import java.time.LocalDate
@Repository
class OffenderIepLevelsRepository(private val createOffenderIepLevels: CreateOffenderIepLevels) : RepositoryBase() {
  fun createOffenderIepLevels(
    offenderBookId: Long,
    toAgencyLocationId: Long,
    movementDate: LocalDate
  ) {
    val params = MapSqlParameterSource()
      .addValue("p_off_book_id", offenderBookId)
      .addValue("p_to_agy_loc_id", toAgencyLocationId)
      .addValue("p_movement_date", movementDate)

    createOffenderIepLevels.execute(params)
  }
}
