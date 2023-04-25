package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.AgencyPrisonerPayProfile
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgyPrisonerPayProfile
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyPrisonerPayProfileRepository
import java.math.RoundingMode
import java.time.LocalDate

@Service
class AgencyPrisonerPayProfileService(
  private val agencyPrisonerPayProfileRepository: AgencyPrisonerPayProfileRepository,
) {
  fun getAgencyPrisonerPayProfile(agencyId: String): AgencyPrisonerPayProfile {
    val today = LocalDate.now()
    val agencyProfilesList = agencyPrisonerPayProfileRepository
      .findAgencyPrisonerPayProfileByAgyLocId(agencyId)
      .filter { agy -> !agy.startDate.isAfter(today) && (agy.endDate == null || !agy.endDate.isBefore(today)) }

    if (agencyProfilesList.isEmpty()) {
      log.error("No AGY_PRISONER_PAY_PROFILES row is active for agency {} on {}", agencyId, today)
      throw(EntityNotFoundException.withId(agencyId))
    }

    return entityToModel(agencyProfilesList.first())
  }

  private fun entityToModel(entity: AgyPrisonerPayProfile) = AgencyPrisonerPayProfile(
    agencyId = entity.agyLocId,
    startDate = entity.startDate,
    endDate = entity.endDate,
    autoPayFlag = entity.autoPayFlag == "Y",
    payFrequency = entity.payFrequency,
    weeklyAbsenceLimit = entity.weeklyAbsenceLimit,
    minHalfDayRate = entity.minHalfDayRate.setScale(2, RoundingMode.HALF_UP),
    maxHalfDayRate = entity.maxHalfDayRate.setScale(2, RoundingMode.HALF_UP),
    maxPieceWorkRate = entity.maxPieceWorkRate.setScale(2, RoundingMode.HALF_UP),
    maxBonusRate = entity.maxBonusRate.setScale(2, RoundingMode.HALF_UP),
    backdateDays = entity.backdateDays,
    defaultPayBandCode = entity.defaultPayBandCode,
  )

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
