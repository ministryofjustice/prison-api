package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository

@Service
@Transactional
class ServiceAgencySwitchesService(
  private val serviceAgencySwitchesRepository: ServiceAgencySwitchesRepository,
  private val externalServiceRepository: ExternalServiceRepository,
) {

  fun getServicePrisons(serviceCode: String): List<PrisonDetails> {
    val service = externalServiceRepository.findByIdOrNull(serviceCode) ?: throw EntityNotFoundException("Service code $serviceCode does not exist")
    return serviceAgencySwitchesRepository.findByIdExternalService(service)
      .map { PrisonDetails(it.id.agencyLocation.id, it.id.agencyLocation.description) }
  }
}
