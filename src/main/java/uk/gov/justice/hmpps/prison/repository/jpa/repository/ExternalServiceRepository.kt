package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.ALL_AGENCIES
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity

interface ExternalServiceRepository : CrudRepository<ExternalServiceEntity, String> {
  fun existsByServiceNameAndServiceAgencySwitchesIdAgencyLocationIdIn(serviceName: String, prisonIds: Collection<String>): Boolean
  fun checkServiceAgencyAndAll(serviceName: String, agencyId: String): Boolean = existsByServiceNameAndServiceAgencySwitchesIdAgencyLocationIdIn(serviceName, setOf(agencyId, ALL_AGENCIES))
}
