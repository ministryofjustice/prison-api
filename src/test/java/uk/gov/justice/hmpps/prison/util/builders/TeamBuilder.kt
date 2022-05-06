package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.util.UUID

class TeamBuilder(
  var code: String? = null,
  var description: String? = null,
  var areaCode: String = "LON",
  var categoryCode: String = "MANAGE",
  var agencyId: String = "MDI",
) {
  fun save(
    dataLoader: DataLoaderRepository,
  ): Team {
    return dataLoader.teamRepository.save(
      Team().apply {
        this.code = code ?: UUID.randomUUID().toString().takeLast(20)
        this.description = description ?: UUID.randomUUID().toString().takeLast(40)
        this.area = dataLoader.institutionAreaRepository.findById(InstitutionArea.pk(areaCode)).orElseThrow()
        this.category = dataLoader.teamCategoryRepository.findById(TeamCategory.pk(categoryCode)).orElseThrow()
        this.isActive = true
        this.listSequence = 1
        this.location = dataLoader.agencyLocationRepository.findById(agencyId).orElseThrow()
        this.queueClusterId = 1
      }
    )
  }
}
