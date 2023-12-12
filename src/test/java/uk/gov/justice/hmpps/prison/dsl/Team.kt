package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.TeamRepository

@DslMarker
annotation class TeamDslMarker

@NomisDataDslMarker
interface TeamDsl

@Component
class TeamBuilderRepository(
  private val teamRepository: TeamRepository,
  private val institutionAreaRepository: ReferenceCodeRepository<InstitutionArea>,
  private val teamCategoryRepository: ReferenceCodeRepository<TeamCategory>,
  private val agencyLocationRepository: AgencyLocationRepository,
) {
  fun save(
    code: String,
    description: String,
    areaCode: String,
    categoryCode: String,
    agencyId: String,
  ): Team =
    teamRepository.save(
      Team().apply {
        this.code = code
        this.description = description
        this.area = institutionAreaRepository.findById(InstitutionArea.pk(areaCode)).orElseThrow()
        this.category = teamCategoryRepository.findById(TeamCategory.pk(categoryCode)).orElseThrow()
        this.isActive = true
        this.listSequence = 1
        this.location = agencyLocationRepository.findById(agencyId).orElseThrow()
        this.queueClusterId = 1
      },
    )
}

@Component
class TeamBuilderFactory(
  private val repository: TeamBuilderRepository,
) {

  fun builder(): TeamBuilder {
    return TeamBuilder(repository)
  }
}

class TeamBuilder(
  private val repository: TeamBuilderRepository,
) : TeamDsl {
  private lateinit var team: Team

  fun build(
    code: String,
    description: String,
    areaCode: String,
    categoryCode: String,
    agencyId: String,
  ): Team = repository.save(
    code = code,
    description = description,
    areaCode = areaCode,
    categoryCode = categoryCode,
    agencyId = agencyId,
  ).also {
    team = it
  }
}
