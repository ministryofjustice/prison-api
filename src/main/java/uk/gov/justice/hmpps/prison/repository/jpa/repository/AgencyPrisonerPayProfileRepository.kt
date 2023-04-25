package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgyPrisonerPayProfile

interface AgencyPrisonerPayProfileRepository : CrudRepository<AgyPrisonerPayProfile, Long> {
  fun findAgencyPrisonerPayProfileByAgyLocId(agencyId: String): List<AgyPrisonerPayProfile>
}
