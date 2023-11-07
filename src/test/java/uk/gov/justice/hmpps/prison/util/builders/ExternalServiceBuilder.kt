package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalServiceEntity
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository

class ExternalServiceBuilder(private val serviceName: String) {
  fun save(
    dataLoader: DataLoaderRepository,
  ) =
    dataLoader.externalServiceRepository.save(
      ExternalServiceEntity(serviceName, serviceName),
    )
}
