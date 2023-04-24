package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgyPrisonerPayProfile
import java.time.LocalDate
import java.util.Optional

interface AgencyPrisonerPayProfileRepository : CrudRepository<AgyPrisonerPayProfile, Long> {
  fun findAgencyPrisonerPayProfileByAgyLocIdEqualsAndEndDateIsNullAndStartDateIsLessThanEqual(
    agencyId: String,
    dateToday: LocalDate,
  ): Optional<AgyPrisonerPayProfile>
}
