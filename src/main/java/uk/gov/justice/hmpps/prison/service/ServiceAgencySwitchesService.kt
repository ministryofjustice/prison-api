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
    findExternalServiceOrThrow(serviceCode)
      .serviceAgencySwitches
      .map { PrisonDetails(it.id.agencyLocation.id, it.id.agencyLocation.description) }

  fun addServicePrison(serviceCode: String, prisonId: String): PrisonDetails {
    val service = findExternalServiceOrThrow(serviceCode)
    val agency = findAgencyLocationOrThrow(prisonId)
    if (service.serviceAgencySwitches.map { it.id.agencyLocation }.contains(agency)) {
      throw ConflictingRequestException("Prison $prisonId is already active for service $serviceCode")
    }
    service.serviceAgencySwitches += ServiceAgencySwitch(ServiceAgencySwitchId(service, agency))
    return PrisonDetails(agency.id, agency.description)
  }

  fun removeServicePrison(serviceCode: String, prisonId: String) {
    val service = findExternalServiceOrThrow(serviceCode)
    val agency = findAgencyLocationOrThrow(prisonId)
    service.serviceAgencySwitches.firstOrNull { it.id.agencyLocation == agency }
      ?.also { service.serviceAgencySwitches -= it }
  }

  private fun findExternalServiceOrThrow(serviceCode: String) =
    externalServiceRepository.findByIdOrNull(serviceCode)
      ?: throw EntityNotFoundException("Service code $serviceCode does not exist")

  private fun findAgencyLocationOrThrow(prisonId: String) =
    agencyLocationRepository.findByIdOrNull(prisonId)
      ?: throw EntityNotFoundException("Prison id $prisonId does not exist")
}
