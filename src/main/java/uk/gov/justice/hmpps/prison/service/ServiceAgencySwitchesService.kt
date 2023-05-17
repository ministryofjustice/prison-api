package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ServiceAgencySwitchesRepository

@Service
@Transactional
class ServiceAgencySwitchesService(
  private val serviceAgencySwitchesRepository: ServiceAgencySwitchesRepository,
  private val externalServiceRepository: ExternalServiceRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
) {

  fun getServicePrisons(serviceCode: String): List<PrisonDetails> {
    val service = externalServiceRepository.findByIdOrNull(serviceCode) ?: throw EntityNotFoundException("Service code $serviceCode does not exist")
    return serviceAgencySwitchesRepository.findByIdExternalService(service)
      .map { PrisonDetails(it.id.agencyLocation.id, it.id.agencyLocation.description) }
  }

  fun addServicePrison(serviceCode: String, prisonId: String) {
    val service = externalServiceRepository.findByIdOrNull(serviceCode) ?: throw EntityNotFoundException("Service code $serviceCode does not exist")
    val agency = agencyLocationRepository.findByIdOrNull(prisonId) ?: throw EntityNotFoundException("Prison id $prisonId does not exist")
    val id = ServiceAgencySwitchId(service, agency)
    serviceAgencySwitchesRepository.findByIdOrNull(id)
      ?.also { throw BadRequestException("Prison $prisonId is already active for service $serviceCode") }
      ?: also { serviceAgencySwitchesRepository.save(ServiceAgencySwitch(id)) }
  }
}
