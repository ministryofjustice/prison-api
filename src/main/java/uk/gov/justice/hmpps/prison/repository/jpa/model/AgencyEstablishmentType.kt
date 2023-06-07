package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import lombok.NoArgsConstructor

@Entity
@DiscriminatorValue(AgencyEstablishmentType.ESTABLISHMENT_TYPE)
@NoArgsConstructor
class AgencyEstablishmentType(code: String?, description: String?) :
  ReferenceCode(ESTABLISHMENT_TYPE, code, description) {

  companion object {
    const val ESTABLISHMENT_TYPE = "ESTAB_TYPE"
    fun pk(code: String?): Pk {
      return Pk(ESTABLISHMENT_TYPE, code)
    }
  }
}
