package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(StaffRole.DOMAIN)
class StaffRole(code: String?, description: String?) : ReferenceCode(DOMAIN, code, description) {
  companion object {
    const val DOMAIN = "STAFF_ROLE"
  }
}
