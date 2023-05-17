package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitch
import uk.gov.justice.hmpps.prison.repository.jpa.model.ServiceAgencySwitchId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalServiceRepository

@Service
@Transactional
class ServiceAgencySwitchesService(
  private val externalServiceRepository: ExternalServiceRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
) {

  fun getServicePrisons(serviceCode: String): List<PrisonDetails> =
    externalServiceRepository.findByIdOrNull(serviceCode)
      ?.serviceAgencySwitches
      ?.map { PrisonDetails(it.id.agencyLocation.id, it.id.agencyLocation.description) }
      ?: throw EntityNotFoundException("Service code $serviceCode does not exist")

  fun addServicePrison(serviceCode: String, prisonId: String): PrisonDetails {
    val service = externalServiceRepository.findByIdOrNull(serviceCode) ?: throw EntityNotFoundException("Service code $serviceCode does not exist")
    val agency = agencyLocationRepository.findByIdOrNull(prisonId) ?: throw EntityNotFoundException("Prison id $prisonId does not exist")
    if (service.serviceAgencySwitches.map { it.id.agencyLocation }.contains(agency)) {
      throw BadRequestException("Prison $prisonId is already active for service $serviceCode")
    }
    service.serviceAgencySwitches.add(ServiceAgencySwitch(ServiceAgencySwitchId(service, agency)))
    return PrisonDetails(agency.id, agency.description)
  }
}
