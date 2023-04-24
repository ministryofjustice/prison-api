package uk.gov.justice.hmpps.prison.service

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
    val result = agencyPrisonerPayProfileRepository
      .findAgencyPrisonerPayProfileByAgyLocIdEqualsAndEndDateIsNullAndStartDateIsLessThanEqual(agencyId, LocalDate.now())
      .orElseThrow(EntityNotFoundException.withId(agencyId))
    return entityToModel(result)
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
}
