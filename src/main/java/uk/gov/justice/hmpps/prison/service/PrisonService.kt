package uk.gov.justice.hmpps.prison.service

import lombok.AllArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.PrisonerMinimumDto
import uk.gov.justice.hmpps.prison.repository.PrisonRepository

@Service
@Transactional
@Slf4j
@AllArgsConstructor
class PrisonService(val prisonRepository: PrisonRepository) {

  fun getPrisonersByEstablishment(establishmentId: String, limit: Long, offset: Long): List<PrisonerMinimumDto> {
    return prisonRepository.findPrisonersByEstablishment(establishmentId, limit, offset)
  }
}
