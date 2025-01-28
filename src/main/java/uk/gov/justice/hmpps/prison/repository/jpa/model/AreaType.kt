package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import lombok.NoArgsConstructor

@Entity
@DiscriminatorValue(AreaType.AREA_TYPE)
@NoArgsConstructor
class AreaType(code: String?, description: String?) : ReferenceCode(AREA_TYPE, code, description) {

  companion object {
    const val AREA_TYPE = "AREA_TYPE"
    val INST: AreaType = AreaType("INST")
    val COMM: AreaType = AreaType("COMM")

    fun pk(code: String?): Pk = Pk(AREA_TYPE, code)
  }

  constructor(code: String) : this(code, null)
}
