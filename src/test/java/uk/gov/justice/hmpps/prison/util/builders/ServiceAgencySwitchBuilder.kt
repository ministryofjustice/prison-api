package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository

class ServiceAgencySwitchBuilder(
  private val serviceName: String,
  private val agencyLocation: String,
) {
  fun save(dataLoader: DataLoaderRepository): ServiceAgencySwitch {
    val externalService = dataLoader.externalServiceRepository.findByIdOrNull(serviceName)
      ?: throw BadRequestException("External service $serviceName not found")
    val agency = dataLoader.agencyLocationRepository.findByIdOrNull(agencyLocation)
      ?: throw BadRequestException("Agency $agencyLocation not found")
    return dataLoader.serviceAgencySwitchesRepository.save(
      ServiceAgencySwitch(
        ServiceAgencySwitchId(
          externalServiceEntity = externalService,
          agencyLocation = agency,
        ),
      ),
    )
  }
}
