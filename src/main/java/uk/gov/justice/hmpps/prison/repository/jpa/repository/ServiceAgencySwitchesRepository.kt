package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId

interface ServiceAgencySwitchesRepository : CrudRepository<ServiceAgencySwitch, ServiceAgencySwitchId> {
  fun findByIdExternalServiceEntity(externalServiceEntity: ExternalServiceEntity): List<ServiceAgencySwitch>
}
