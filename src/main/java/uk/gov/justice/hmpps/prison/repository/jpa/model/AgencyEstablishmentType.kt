package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(AgencyEstablishmentType.Companion.ESTABLISHMENT_TYPE)
class AgencyEstablishmentType(code: String?, description: String?) : ReferenceCode(AgencyEstablishmentType.Companion.ESTABLISHMENT_TYPE, code, description) {

  companion object {
    const val ESTABLISHMENT_TYPE = "ESTAB_TYPE"
    fun pk(code: String?): Pk = Pk(ESTABLISHMENT_TYPE, code)
  }
}
