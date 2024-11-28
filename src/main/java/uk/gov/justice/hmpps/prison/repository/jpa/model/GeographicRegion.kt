package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import lombok.NoArgsConstructor

@Entity
@DiscriminatorValue(GeographicRegion.GEOGRAPHIC)
@NoArgsConstructor
class GeographicRegion(code: String?, description: String?) :
  ReferenceCode(GEOGRAPHIC, code, description) {

  companion object {
    const val GEOGRAPHIC: String = "GEOGRAPHIC"
    fun pk(code: String?): Pk {
      return Pk(GEOGRAPHIC, code)
    }
  }
}
