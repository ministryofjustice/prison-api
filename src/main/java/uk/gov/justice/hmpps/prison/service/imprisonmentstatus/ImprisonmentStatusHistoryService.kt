package uk.gov.justice.hmpps.prison.service.imprisonmentstatus

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.imprisonmentstatus.ImprisonmentStatusHistoryDto
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository

@Service
@Transactional(readOnly = true)
class ImprisonmentStatusHistoryService(
  private val offenderImprisonmentStatusRepository: OffenderImprisonmentStatusRepository,
) {

  fun getImprisonmentStatusHistory(offenderNo: String): List<ImprisonmentStatusHistoryDto> = offenderImprisonmentStatusRepository.findByOffender(offenderNo)
    .groupBy { it.effectiveDate }
    .map { (_, statuses) -> statuses.maxBy { it.imprisonStatusSeq } }
    .map {
      ImprisonmentStatusHistoryDto(
        status = it.imprisonmentStatus.status,
        effectiveDate = it.effectiveDate,
        agencyId = it.agyLocId,
      )
    }
}
